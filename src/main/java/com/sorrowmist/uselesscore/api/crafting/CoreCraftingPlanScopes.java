package com.sorrowmist.uselesscore.api.crafting;

import appeng.api.networking.crafting.ICraftingPlan;
import java.util.ArrayDeque;
import java.util.Deque;

/** A synchronous confirmation scope protects the exact native execution vector during submission. */
public final class CoreCraftingPlanScopes {
    private static final ThreadLocal<Deque<Scope>> CONFIRMED = ThreadLocal.withInitial(ArrayDeque::new);
    private CoreCraftingPlanScopes() {}

    public static Scope confirm(ICraftingPlan plan) {
        Scope scope = new Scope(java.util.Objects.requireNonNull(plan));
        CONFIRMED.get().push(scope);
        return scope;
    }

    public static boolean shouldPreserveSubmissionPlan(ICraftingPlan plan) {
        Scope scope = CONFIRMED.get().peek();
        return plan != null && scope != null && scope.plan == plan;
    }

    public static final class Scope implements AutoCloseable {
        private final ICraftingPlan plan;
        private boolean closed;
        private Scope(ICraftingPlan plan) { this.plan = plan; }
        @Override public void close() {
            if (closed) return;
            Deque<Scope> stack = CONFIRMED.get();
            if (stack.peek() != this) throw new IllegalStateException("Crafting confirmation scopes must close in order");
            stack.pop();
            if (stack.isEmpty()) CONFIRMED.remove();
            closed = true;
        }
    }
}
