package com.sorrowmist.uselesscore.api.recipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
/** Addon hooks for recipe representatives and mold matching; no mixin into another mod is needed. */
public final class CoreRecipeHooks {
    private static final CopyOnWriteArrayList<BiFunction<Ingredient,ItemStack,ItemStack>> REPRESENTATIVES = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<BiFunction<Ingredient,ItemStack,Boolean>> MOLDS = new CopyOnWriteArrayList<>();
    private CoreRecipeHooks() {}
    public static void registerRepresentative(BiFunction<Ingredient,ItemStack,ItemStack> hook) { REPRESENTATIVES.add(Objects.requireNonNull(hook)); }
    /** A null result means that this hook does not handle the mold. */
    public static void registerMoldMatcher(BiFunction<Ingredient,ItemStack,Boolean> hook) { MOLDS.add(Objects.requireNonNull(hook)); }
    public static ItemStack representative(Ingredient ingredient, ItemStack fallback) {
        ItemStack result=fallback;
        for(var hook:REPRESENTATIVES) {
            var preferred=hook.apply(ingredient,result);
            if(preferred!=null && !preferred.isEmpty() && ingredient!=null && ingredient.test(preferred)) result=preferred.copyWithCount(1);
        }
        var context=com.sorrowmist.uselesscore.api.ae.CoreAeMaterialContext.current();
        return context==null ? result : context.pickItem(ingredient,result);
    }
    public static Boolean matchMold(Ingredient required, ItemStack actual) {
        for(var hook:MOLDS) { Boolean result=hook.apply(required,actual); if(result!=null)return result; }
        return null;
    }
}
