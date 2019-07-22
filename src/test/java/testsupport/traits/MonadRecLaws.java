package testsupport.traits;

import com.jnape.palatable.lambda.monad.MonadRec;
import com.jnape.palatable.traitor.traits.Trait;

import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.recurse;
import static com.jnape.palatable.lambda.functions.recursion.RecursiveResult.terminate;
import static testsupport.Constants.STACK_EXPLODING_NUMBER;

public class MonadRecLaws<M extends MonadRec<?, M>> implements Trait<MonadRec<?, M>> {

    @Override
    public void test(MonadRec<?, M> monadRec) {
        MonadRec<Integer, M> expected = monadRec.pure(STACK_EXPLODING_NUMBER);
        MonadRec<Integer, M> actual = monadRec.pure(0).trampolineM(x -> monadRec.pure(x < STACK_EXPLODING_NUMBER
                                                                                          ? recurse(x + 1)
                                                                                          : terminate(x)));
        if (!expected.equals(actual))
            throw new AssertionError("trampolineM: " + actual + " /= " + expected);

    }
}
