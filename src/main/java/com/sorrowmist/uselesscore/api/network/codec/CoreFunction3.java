package com.sorrowmist.uselesscore.api.network.codec;

@FunctionalInterface
public interface CoreFunction3<A, B, C, R> {
    R apply(A a, B b, C c);
}
