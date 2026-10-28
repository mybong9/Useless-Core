package com.sorrowmist.uselesscore.api.crafting;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;

import java.util.Objects;
import java.util.UUID;

/** Atomic output claims distinguish the planned key from the actual stack's NBT. */
public final class CoreCraftingOutput {
    public record Request(UUID craftingJobId, AEKey expectedKey, AEKey actualKey, long amount, Actionable mode) {
        public Request {
            Objects.requireNonNull(craftingJobId);
            Objects.requireNonNull(expectedKey);
            Objects.requireNonNull(actualKey);
            Objects.requireNonNull(mode);
            if (amount <= 0) throw new IllegalArgumentException("Claim amount must be positive");
        }
    }

    public record Result(boolean accepted, long claimedAmount) {
        public static final Result REJECTED = new Result(false, 0);
        public Result {
            if (claimedAmount < 0 || !accepted && claimedAmount != 0) throw new IllegalArgumentException("Invalid claim result");
        }
    }

    public interface Target { Result claimCoreCraftingOutput(Request request); }

    private CoreCraftingOutput() {}

    public static Result claim(Object cpu, Request request) {
        return cpu instanceof Target target ? target.claimCoreCraftingOutput(request) : Result.REJECTED;
    }
}
