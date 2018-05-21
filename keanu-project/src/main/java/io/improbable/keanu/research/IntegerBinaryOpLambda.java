package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.NonProbabilisticInteger;

import java.util.function.BiFunction;

public class IntegerBinaryOpLambda<A, B> extends NonProbabilisticInteger {

    protected final Vertex<A> a;
    protected final Vertex<B> b;
    protected final BiFunction<A, B, Integer> op;

    public IntegerBinaryOpLambda(Vertex<A> a,
                                 Vertex<B> b,
                                 BiFunction<A, B, Integer> op) {
        this.a = a;
        this.b = b;
        this.op = op;
        setParents(a, b);
    }

    @Override
    public Integer sample() {
        return op.apply(a.sample(), b.sample());
    }

    @Override
    public Integer getDerivedValue() {
        return op.apply(a.getValue(), b.getValue());
    }
}
