package com.sorrowmist.uselesscore.api.network.codec;

@FunctionalInterface
public interface CoreFunction4<A, B, C, D, R> {
    R apply(A a, B b, C c, D d);
}
