package com.sorrowmist.uselesscore.api.ae;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;

import java.util.Arrays;
import java.util.List;

/** Shared views of the array-based AE2 15 pattern API used on Forge 1.20.1. */
public final class CoreAePatterns {
    private CoreAePatterns() {}

    public static List<GenericStack> outputs(IPatternDetails pattern) {
        return Arrays.asList(pattern.getOutputs());
    }
}
