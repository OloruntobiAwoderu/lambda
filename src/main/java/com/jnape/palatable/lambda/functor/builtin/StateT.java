package com.jnape.palatable.lambda.functor.builtin;

import com.jnape.palatable.lambda.adt.Unit;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.recursion.RecursiveResult;
import com.jnape.palatable.lambda.functor.Applicative;
import com.jnape.palatable.lambda.monad.Monad;
import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.lambda.monad.transformer.MonadT;

import static com.jnape.palatable.lambda.adt.Unit.UNIT;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Into.into;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.lambda.functor.builtin.State.Tuple2T.tuple2T;
import static com.jnape.palatable.lambda.functor.builtin.StateT.stateT;

public interface StateT<S, M extends MonadRec<?, M>, A> extends
    MonadT<Fn1<S, ?>, State.Tuple2T<M, S, ?>, A, StateT<S, M, ?>>,
    MonadRec<A, StateT<S, M, ?>> {

    MonadRec<Tuple2<S, A>, M> runStateT(S s);

    @Override
    default <GA extends Monad<A, State.Tuple2T<M, S, ?>>, FGA extends Monad<GA, Fn1<S, ?>>> FGA run() {
        return Fn1.<S, GA>fn1(s -> tuple2T(runStateT(s)).coerce()).coerce();
    }

    @Override
    default <B> StateT<S, M, B> flatMap(Fn1<? super A, ? extends Monad<B, StateT<S, M, ?>>> f) {
        return s -> runStateT(s).flatMap(into((s_, a) -> f.apply(a).<StateT<S, M, B>>coerce().runStateT(s_)));
    }

    @Override
    default <B> StateT<S, M, B> pure(B b) {
        return s -> runStateT(s).pure(tuple(s, b));
    }

    @Override
    default <B> StateT<S, M, B> fmap(Fn1<? super A, ? extends B> fn) {
        return MonadRec.super.<B>fmap(fn).coerce();
    }

    @Override
    default <B> StateT<S, M, B> zip(Applicative<Fn1<? super A, ? extends B>, StateT<S, M, ?>> appFn) {
        return s -> new Compose<>(runStateT(s))
            .zip(new Compose<>(appFn.<StateT<S, M, Fn1<? super A, ? extends B>>>coerce().runStateT(s)))
            .getCompose();
    }

    @Override
    default <B> Lazy<StateT<S, M, B>> lazyZip(
        Lazy<? extends Applicative<Fn1<? super A, ? extends B>, StateT<S, M, ?>>> lazyAppFn) {
        return MonadRec.super.lazyZip(lazyAppFn).fmap(MonadRec<B, StateT<S, M, ?>>::coerce);
    }

    @Override
    default <B> StateT<S, M, B> discardL(Applicative<B, StateT<S, M, ?>> appB) {
        return MonadRec.super.discardL(appB).coerce();
    }

    @Override
    default <B> StateT<S, M, A> discardR(Applicative<B, StateT<S, M, ?>> appB) {
        return MonadRec.super.discardR(appB).coerce();
    }

    @Override
    default <B> StateT<S, M, B> trampolineM(
        Fn1<? super A, ? extends MonadRec<RecursiveResult<A, B>, StateT<S, M, ?>>> fn) {
        return stateT(s -> runStateT(s)
            .trampolineM(into((s_, a) -> fn.apply(a).<StateT<S, M, RecursiveResult<A, B>>>coerce().runStateT(s_)
                .fmap(into((s__, aOrB) -> aOrB.biMap(a_ -> tuple(s__, a_), b -> tuple(s__, b)))))));
    }

    static <S, M extends MonadRec<?, M>, A> StateT<S, M, A> stateT(
        Fn1<? super S, ? extends MonadRec<Tuple2<S, A>, M>> stateFn) {
        return stateFn::apply;
    }

    static <S, M extends MonadRec<?, M>, A> StateT<S, M, A> gets(Fn1<? super S, ? extends MonadRec<A, M>> sma) {
        return s -> sma.apply(s).fmap(tupler(s));
    }

    static <S, M extends MonadRec<?, M>> StateT<S, M, Unit> modify(Fn1<? super S, ? extends MonadRec<S, M>> fn) {
        return s -> fn.apply(s).fmap(s_ -> tuple(s_, UNIT));
    }
}
