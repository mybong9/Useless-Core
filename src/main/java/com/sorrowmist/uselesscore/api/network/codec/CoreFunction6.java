package com.sorrowmist.uselesscore.api.network.codec;

@FunctionalInterface
public interface CoreFunction6<A, B, C, D, E, F, R> {
    R apply(A a, B b, C c, D d, E e, F f);
}
