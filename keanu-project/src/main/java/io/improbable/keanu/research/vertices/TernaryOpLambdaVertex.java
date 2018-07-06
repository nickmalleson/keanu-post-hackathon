package io.improbable.keanu.research.vertices;

import io.improbable.keanu.research.TriFunction;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

public class TernaryOpLambdaVertex<A,B,C,R> extends NonProbabilistic<R> {


    protected final Vertex<A> a;
    protected final Vertex<B> b;
    protected final Vertex<C> c;
    private TriFunction<A, B, C, R> op;

    public TernaryOpLambdaVertex(Vertex<A> a, Vertex<B> b, Vertex<C> c, TriFunction<A, B, C, R> op) {
        this.a = a;
        this.b = b;
        this.c = c;
        setParents(a, b, c);
        this.op = op;
    }

    @Override
    public R sample(KeanuRandom random) {
        return op.apply(a.sample(random), b.sample(random), c.sample(random));
    }

    @Override
    public R getDerivedValue() {
        return op.apply(a.getValue(), b.getValue(), c.getValue());
    }


}
