package com.jnape.palatable.lambda.functions.builtin.dyadic;

import com.jnape.palatable.lambda.adt.tuples.Tuple2;
import com.jnape.palatable.lambda.functions.DyadicFunction;
import com.jnape.palatable.lambda.functions.MonadicFunction;
import com.jnape.palatable.lambda.iterators.UnfoldingIterator;

import java.util.Optional;

/**
 * Given an initial seed value and a function that takes the seed type and produces an <code>Optional&lt;{@link
 * Tuple2}&lt;X, Seed&gt;&gt;</code>, where the tuple's first slot represents the next <code>Iterable</code> element,
 * and the second slot represents the next input to the unfolding function, unfold an <code>Iterable</code> of
 * <code>X</code>s. Returning <code>Optional.empty()</code> from the unfolding function is a signal that the
 * <code>Iterable</code> is fully unfolded.
 * <p>
 * For more information, read about <a href="https://en.wikipedia.org/wiki/Anamorphism" target="_top">Anamorphisms</a>.
 * <p>
 * Example:
 * <pre>
 * {@code
 * Iterable<Integer> zeroThroughTenInclusive = unfoldr(x -> x <= 10
 *         ? Optional.of(tuple(x, x + 1))
 *         : Optional.empty(), 0);
 * }
 * </pre>
 *
 * @param <A> The output Iterable element type
 * @param <B> The unfolding function input type
 */
public final class Unfoldr<A, B> implements DyadicFunction<MonadicFunction<B, Optional<Tuple2<A, B>>>, B, Iterable<A>> {

    private Unfoldr() {
    }

    @Override
    public Iterable<A> apply(MonadicFunction<B, Optional<Tuple2<A, B>>> function, B b) {
        return () -> new UnfoldingIterator<>(function, b);
    }

    public static <A, B> Unfoldr<A, B> unfoldr() {
        return new Unfoldr<>();
    }

    public static <A, B> MonadicFunction<B, Iterable<A>> unfoldr(MonadicFunction<B, Optional<Tuple2<A, B>>> function) {
        return Unfoldr.<A, B>unfoldr().apply(function);
    }

    public static <A, B> Iterable<A> unfoldr(MonadicFunction<B, Optional<Tuple2<A, B>>> function, B b) {
        return unfoldr(function).apply(b);
    }
}
