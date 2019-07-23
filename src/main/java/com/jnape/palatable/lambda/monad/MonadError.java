package com.jnape.palatable.lambda.monad;

import com.jnape.palatable.lambda.adt.Either;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.functor.builtin.Lazy;
import com.jnape.palatable.lambda.io.IO;

import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.monad.Monad.join;

/**
 * An interface for {@link Monad monads} that can be interrupted with some type of error. The type of error is fully
 * dictated by the instance of {@link MonadError} and is not necessarily analogous to Java {@link Exception exceptions}
 * or even {@link Throwable}. For instance, {@link IO} can be thrown any {@link Throwable}, where as {@link Either} can
 * only be "thrown" a value of its {@link Either#left(Object) left} type.
 *
 * @param <E> the error type
 * @param <M> the {@link Monad} witness
 * @param <A> the carrier
 */
public interface MonadError<E, A, M extends Monad<?, M>> extends Monad<A, M> {

    /**
     * Throw an error value of type <code>E</code> into the {@link Monad monad}.
     *
     * @param e the error type
     * @return the {@link Monad monad}
     */
    <B> MonadError<E, B, M> throwError(E e);

    /**
     * Catch any {@link MonadError#throwError(Object) thrown} errors inside the {@link Monad} and resume normal
     * operations.
     *
     * @param recoveryFn the catch function
     * @return the recovered {@link Monad}
     */
    MonadError<E, A, M> catchError(Fn1<? super E, ? extends Monad<A, M>> recoveryFn);

    /**
     * Ensure that a monadic effect runs after this effect regardless of whether or not this effect threw an error
     * (analogous to <code>finally</code>).
     *
     * @param mb  the effect to run after this effect
     * @param <B> the ensured effect's carrier type
     * @return the combination of this effect and the effect to run after
     */
    default <B> MonadError<E, A, M> ensuring(MonadError<E, B, M> mb) {
        return join(fmap(a -> mb.fmap(constantly(a)))
                        .catchError(e -> mb
                            .catchError(constantly(throwError(e)))
                            .fmap(constantly(throwError(e)))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    <B> MonadError<E, B, M> flatMap(Fn1<? super A, ? extends Monad<B, M>> f);

    /**
     * {@inheritDoc}
     */
    @Override
    <B> MonadError<E, B, M> pure(B b);

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadError<E, B, M> fmap(Fn1<? super A, ? extends B> fn) {
        return Monad.super.<B>fmap(fn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadError<E, B, M> zip(Applicative<Fn1<? super A, ? extends B>, M> appFn) {
        return Monad.super.zip(appFn).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> Lazy<? extends MonadError<E, B, M>> lazyZip(
        Lazy<? extends Applicative<Fn1<? super A, ? extends B>, M>> lazyAppFn) {
        return Monad.super.lazyZip(lazyAppFn).fmap(Monad<B, M>::coerce);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadError<E, B, M> discardL(Applicative<B, M> appB) {
        return Monad.super.discardL(appB).coerce();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <B> MonadError<E, A, M> discardR(Applicative<B, M> appB) {
        return Monad.super.discardR(appB).coerce();
    }
}
