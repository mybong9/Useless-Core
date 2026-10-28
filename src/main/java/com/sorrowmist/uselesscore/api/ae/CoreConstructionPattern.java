package com.sorrowmist.uselesscore.api.ae;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
/** AE preparation order: return the exact input materials to ME before releasing its receipt. */
public interface CoreConstructionPattern extends IPatternDetails {
    boolean matches(KeyCounter[] supplied,long copies);
    GenericStack[] materials(long copies);
}
