package com.jnape.palatable.lambda.monad;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.builtin.Lazy;

public interface MonadRec<A, M extends MonadRec<?, M>> extends Monad<A, M> {

    /**
     * Internally trampoline a function
     * <code>a -&gt; {@link MonadRec}&lt;{@link RecursiveResult}&lt;A, B&gt;, M&gt;</code> such that a constant
     * factor of stack space is consumed regardless of the number of times it bounces.
     *
     * @param fn  the function to trampoline
     * @param <B> the new carrier value
     * @return the {@link MonadRec} after trampolining the function
     */
    <B> MonadRec<B, M> trampolineM(Fn1<? super A, ? extends MonadRec<RecursiveResult<A, B>, M>> fn);

    /**
     * {@inheritDoc}
     */
    @Override
    <B> MonadRec<B, M> flatMap(Fn1<? super A, ? extends Monad<B, M>> f);

    /**
     * {@inheritDoc}
     */
    @Override
    <B> MonadRec<B, M> pure(B b);

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadRec<B, M> fmap(Fn1<? super A, ? extends B> fn) {
        return Monad.super.<B>fmap(fn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadRec<B, M> zip(Applicative<Fn1<? super A, ? extends B>, M> appFn) {
        return Monad.super.zip(appFn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> Lazy<? extends MonadRec<B, M>> lazyZip(Lazy<? extends Applicative<Fn1<? super A, ? extends B>, M>> lazyAppFn) {
        return Monad.super.lazyZip(lazyAppFn).fmap(Monad<B, M>::coerce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadRec<B, M> discardL(Applicative<B, M> appB) {
        return Monad.super.discardL(appB).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadRec<A, M> discardR(Applicative<B, M> appB) {
        return Monad.super.discardR(appB).coerce();
    }
}
