package com.sorrowmist.uselesscore.api.ae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.crafting.pattern.EncodedPatternItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.function.BiFunction;

/** Native AE2 1.20.1 encoded pattern item with a caller-supplied decoder. */
public final class CoreEncodedPatternItem extends EncodedPatternItem {
    private final BiFunction<AEItemKey, Level, ? extends IPatternDetails> decoder;
    public CoreEncodedPatternItem(Properties properties,
            BiFunction<AEItemKey, Level, ? extends IPatternDetails> decoder) {
        super(properties);
        this.decoder = decoder;
    }
    @Override public IPatternDetails decode(ItemStack stack, Level level, boolean recovery) {
        return decode(AEItemKey.of(stack), level);
    }
    @Override public IPatternDetails decode(AEItemKey key, Level level) {
        return decoder.apply(key, level);
    }
}
