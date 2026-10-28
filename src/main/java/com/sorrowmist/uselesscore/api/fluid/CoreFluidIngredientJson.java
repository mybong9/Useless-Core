package com.sorrowmist.uselesscore.api.fluid;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;
/** Converts Forge-style fluid/tag/NBT predicates without exposing another mod's ingredient classes. */
public final class CoreFluidIngredientJson {
    private CoreFluidIngredientJson() {}
    public static FluidIngredient read(JsonElement json) {
        if (json == null || json.isJsonNull()) return FluidIngredient.anyOf(java.util.List.of());
        if (json.isJsonArray()) {
            var parts = new ArrayList<FluidIngredient>();
            for (var child : json.getAsJsonArray()) parts.add(read(child));
            return FluidIngredient.anyOf(parts);
        }
        var object=json.getAsJsonObject();
        FluidIngredient result;
        if(object.has("tag")) result=FluidIngredient.tag(TagKey.create(Registries.FLUID,new ResourceLocation(object.get("tag").getAsString())));
        else if(object.has("fluid")) {
            var id=new ResourceLocation(object.get("fluid").getAsString());
            if(!BuiltInRegistries.FLUID.containsKey(id)) throw new IllegalArgumentException("Unknown fluid: "+id);
            result=FluidIngredient.single(BuiltInRegistries.FLUID.get(id));
        } else return FluidIngredient.anyOf(java.util.List.of());
        if(!object.has("nbt")) return result;
        var nbt=CraftingHelper.getNBT(object.get("nbt"));
        boolean strict=!object.has("strict") || object.get("strict").getAsBoolean();
        var variants=new ArrayList<FluidIngredient>();
        for(FluidStack stack:result.getStacks()) {
            stack.setTag(nbt.copy()); variants.add(DataComponentFluidIngredient.of(strict,stack));
        }
        return FluidIngredient.anyOf(variants);
    }
}
