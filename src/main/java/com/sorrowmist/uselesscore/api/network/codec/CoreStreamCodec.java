package com.sorrowmist.uselesscore.api.network.codec;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;

/** Minimal 1.20.1-friendly equivalent of the 1.21 StreamCodec contract. */
public interface CoreStreamCodec<B, V> {
    V decode(B buffer);

    void encode(B buffer, V value);

    static <B, V> CoreStreamCodec<B, V> unit(V value) {
        java.util.Objects.requireNonNull(value, "value");
        return of((buffer, actual) -> {
            if (!value.equals(actual)) throw new IllegalArgumentException("Unexpected value for constant packet codec");
        }, buffer -> value);
    }

    default <R> R apply(Function<CoreStreamCodec<B, V>, R> operation) {
        return operation.apply(this);
    }

    static <B, V> CoreStreamCodec<B, V> of(BiConsumer<B, V> encoder, Function<B, V> decoder) {
        return new CoreStreamCodec<>() {
            @Override public V decode(B buffer) { return decoder.apply(buffer); }
            @Override public void encode(B buffer, V value) { encoder.accept(buffer, value); }
        };
    }

    default <R> CoreStreamCodec<B, R> map(Function<V, R> decode, Function<R, V> encode) {
        return CoreStreamCodec.of((buffer, value) -> this.encode(buffer, encode.apply(value)),
                buffer -> decode.apply(this.decode(buffer)));
    }

    static <B, A, R> CoreStreamCodec<B, R> composite(
            CoreStreamCodec<B, A> a, Function<R, A> getA, Function<A, R> make) {
        return of((buf, value) -> a.encode(buf, getA.apply(value)), buf -> make.apply(a.decode(buf)));
    }

    static <B, A, C, R> CoreStreamCodec<B, R> composite(
            CoreStreamCodec<B, A> a, Function<R, A> getA,
            CoreStreamCodec<B, C> c, Function<R, C> getC,
            BiFunction<A, C, R> make) {
        return of((buf, value) -> { a.encode(buf, getA.apply(value)); c.encode(buf, getC.apply(value)); },
                buf -> make.apply(a.decode(buf), c.decode(buf)));
    }

    static <B, A, C, D, R> CoreStreamCodec<B, R> composite(
            CoreStreamCodec<B, A> a, Function<R, A> getA,
            CoreStreamCodec<B, C> c, Function<R, C> getC,
            CoreStreamCodec<B, D> d, Function<R, D> getD,
            CoreFunction3<A, C, D, R> make) {
        return of((buf, value) -> { a.encode(buf, getA.apply(value)); c.encode(buf, getC.apply(value)); d.encode(buf, getD.apply(value)); },
                buf -> make.apply(a.decode(buf), c.decode(buf), d.decode(buf)));
    }

    static <B, A, C, D, E, R> CoreStreamCodec<B, R> composite(
            CoreStreamCodec<B, A> a, Function<R, A> getA,
            CoreStreamCodec<B, C> c, Function<R, C> getC,
            CoreStreamCodec<B, D> d, Function<R, D> getD,
            CoreStreamCodec<B, E> e, Function<R, E> getE,
            CoreFunction4<A, C, D, E, R> make) {
        return of((buf, value) -> { a.encode(buf, getA.apply(value)); c.encode(buf, getC.apply(value)); d.encode(buf, getD.apply(value)); e.encode(buf, getE.apply(value)); },
                buf -> make.apply(a.decode(buf), c.decode(buf), d.decode(buf), e.decode(buf)));
    }

    static <B, A, C, D, E, F, R> CoreStreamCodec<B, R> composite(
            CoreStreamCodec<B, A> a, Function<R, A> getA,
            CoreStreamCodec<B, C> c, Function<R, C> getC,
            CoreStreamCodec<B, D> d, Function<R, D> getD,
            CoreStreamCodec<B, E> e, Function<R, E> getE,
            CoreStreamCodec<B, F> f, Function<R, F> getF,
            CoreFunction5<A, C, D, E, F, R> make) {
        return of((buf, value) -> { a.encode(buf, getA.apply(value)); c.encode(buf, getC.apply(value)); d.encode(buf, getD.apply(value)); e.encode(buf, getE.apply(value)); f.encode(buf, getF.apply(value)); },
                buf -> make.apply(a.decode(buf), c.decode(buf), d.decode(buf), e.decode(buf), f.decode(buf)));
    }

    static <B, A, C, D, E, F, G, R> CoreStreamCodec<B, R> composite(
            CoreStreamCodec<B, A> a, Function<R, A> getA,
            CoreStreamCodec<B, C> c, Function<R, C> getC,
            CoreStreamCodec<B, D> d, Function<R, D> getD,
            CoreStreamCodec<B, E> e, Function<R, E> getE,
            CoreStreamCodec<B, F> f, Function<R, F> getF,
            CoreStreamCodec<B, G> g, Function<R, G> getG,
            CoreFunction6<A, C, D, E, F, G, R> make) {
        return of((buf, value) -> { a.encode(buf, getA.apply(value)); c.encode(buf, getC.apply(value)); d.encode(buf, getD.apply(value)); e.encode(buf, getE.apply(value)); f.encode(buf, getF.apply(value)); g.encode(buf, getG.apply(value)); },
                buf -> make.apply(a.decode(buf), c.decode(buf), d.decode(buf), e.decode(buf), f.decode(buf), g.decode(buf)));
    }
}
