package com.sorrowmist.uselesscore.api.network.codec;

@FunctionalInterface
public interface CoreFunction5<A, B, C, D, E, R> {
    R apply(A a, B b, C c, D d, E e);
}
