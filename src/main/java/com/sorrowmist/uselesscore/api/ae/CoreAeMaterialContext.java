package com.sorrowmist.uselesscore.api.ae;

import appeng.api.networking.IGrid;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEFluidKey;
import com.sorrowmist.uselesscore.api.fluid.FluidIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import java.util.*;
import java.util.function.Predicate;

/** One encoding operation's immutable AE stock/craftability snapshot, shared by terminals and addons. */
public final class CoreAeMaterialContext {
    private static final ThreadLocal<CoreAeMaterialContext> CURRENT = new ThreadLocal<>();
    private final Set<AEKey> craftable;
    private final Map<AEKey,Long> stock = new LinkedHashMap<>();
    private final Set<AEKey> all = new LinkedHashSet<>();
    private final Map<net.minecraft.world.item.Item,List<AEKey>> byItem = new HashMap<>();
    private final Map<net.minecraft.world.level.material.Fluid,List<AEKey>> byFluid = new HashMap<>();
    private final Map<Ingredient,ItemStack> items = new IdentityHashMap<>();
    private final Map<FluidIngredient,FluidStack> fluids = new IdentityHashMap<>();
    private CoreAeMaterialContext(IGrid grid) {
        craftable = Set.copyOf(grid.getCraftingService().getCraftables(key -> true));
        for (var entry : grid.getStorageService().getCachedInventory())
            if(entry.getLongValue()>0) stock.put(entry.getKey(),entry.getLongValue());
        all.addAll(craftable); all.addAll(stock.keySet());
        for(var key:all) {
            if(key instanceof AEItemKey item) byItem.computeIfAbsent(item.toStack(1).getItem(),ignored->new ArrayList<>()).add(key);
            else if(key instanceof AEFluidKey fluid) byFluid.computeIfAbsent(fluid.toStack(1).getFluid(),ignored->new ArrayList<>()).add(key);
        }
    }
    public static CoreAeMaterialContext fromGrid(IGrid grid) { return grid==null ? null : new CoreAeMaterialContext(grid); }
    public static CoreAeMaterialContext current() { return CURRENT.get(); }
    public static Scope enter(CoreAeMaterialContext context) {
        var previous=CURRENT.get();
        if(context==null) CURRENT.remove(); else CURRENT.set(context);
        return new Scope(previous);
    }
    public static final class Scope implements AutoCloseable {
        private final CoreAeMaterialContext previous;
        private boolean closed;
        private Scope(CoreAeMaterialContext previous) { this.previous=previous; }
        @Override public void close() {
            if(closed)return; closed=true;
            if(previous==null)CURRENT.remove();else CURRENT.set(previous);
        }
    }
    private AEKey select(Collection<AEKey> candidates, Predicate<AEKey> accepts) {
        AEKey selected = best(candidates, key -> craftable.contains(key) && accepts.test(key));
        return selected!=null ? selected : best(candidates,key -> stock.containsKey(key) && accepts.test(key));
    }
    private Collection<AEKey> itemCandidates(Ingredient ingredient) {
        if(!ingredient.isSimple())return all;
        ItemStack[] displayed;
        try { displayed=ingredient.getItems(); }catch(RuntimeException unsupported){return all;}
        if(displayed.length==0)return all;
        var candidates=new LinkedHashSet<AEKey>();
        for(var stack:displayed) candidates.addAll(byItem.getOrDefault(stack.getItem(),List.of()));
        return candidates;
    }
    private Collection<AEKey> fluidCandidates(FluidIngredient ingredient) {
        if(!ingredient.isSimple())return all;
        var displayed=ingredient.getStacks();
        if(displayed.length==0)return all;
        var candidates=new LinkedHashSet<AEKey>();
        for(var stack:displayed)candidates.addAll(byFluid.getOrDefault(stack.getFluid(),List.of()));
        return candidates;
    }
    private AEKey best(Collection<AEKey> candidates, Predicate<AEKey> accepts) {
        AEKey best=null;long amount=-1;
        for(var key:candidates) {
            boolean valid;
            try {valid=accepts.test(key);}catch(RuntimeException unsupported){continue;}
            if(!valid)continue;
            long count=stock.getOrDefault(key,0L);
            if(best==null || count>amount || count==amount && key.toString().compareTo(best.toString())<0) {
                best=key;amount=count;
            }
        }
        return best;
    }
    public ItemStack pickItem(Ingredient ingredient, ItemStack fallback) {
        if(ingredient==null)return fallback;
        var selected=items.computeIfAbsent(ingredient, required -> {
            var key=select(itemCandidates(required), candidate -> candidate instanceof AEItemKey item && required.test(item.toStack(1)));
            return key instanceof AEItemKey item ? item.toStack(1) : ItemStack.EMPTY;
        });
        return selected.isEmpty()?fallback:selected.copyWithCount(1);
    }
    public FluidStack pickFluid(FluidIngredient ingredient, FluidStack fallback) {
        if(ingredient==null)return fallback;
        var selected=fluids.computeIfAbsent(ingredient, required -> {
            var key=select(fluidCandidates(required), candidate -> candidate instanceof AEFluidKey fluid && required.test(fluid.toStack(1)));
            return key instanceof AEFluidKey fluid ? fluid.toStack(1) : FluidStack.EMPTY;
        });
        return selected.isEmpty()?fallback:selected.copy();
    }
}
