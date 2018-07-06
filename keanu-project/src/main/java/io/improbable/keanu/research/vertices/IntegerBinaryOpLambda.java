package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.NonProbabilisticInteger;

import java.util.function.BiFunction;

public class IntegerBinaryOpLambda<A, B> extends NonProbabilisticInteger {

    protected final Vertex<A> a;
    protected final Vertex<B> b;
    protected final BiFunction<A, B, IntegerTensor> op;

    public IntegerBinaryOpLambda(Vertex<A> a,
                                 Vertex<B> b,
                                 BiFunction<A, B, IntegerTensor> op) {
        this.a = a;
        this.b = b;
        this.op = op;
        setParents(a, b);
    }

    @Override
    public IntegerTensor sample(KeanuRandom random) {
        return op.apply(a.sample(random), b.sample(random));
    }

    @Override
    public IntegerTensor getDerivedValue() {
        return op.apply(a.getValue(), b.getValue());
    }
}
