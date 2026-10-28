package com.sorrowmist.uselesscore.api.component;

import java.util.Objects;
import java.util.function.Supplier;

/** Stable holder type used by mods to share statically declared core components. */
public final class CoreDataComponentHolder<T> implements Supplier<CoreDataComponentType<T>> {
    private final CoreDataComponentType<T> value;

    public CoreDataComponentHolder(CoreDataComponentType<T> value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public CoreDataComponentType<T> get() {
        return value;
    }
}
