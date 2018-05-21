package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;

public class QuadOpLambda<A, B, C, D, O> extends QuadOpVertex<A, B, C, D, O> {

    private QuadFunction<A, B, C, D, O> op;

    public QuadOpLambda(Vertex<A> a, Vertex<B> b, Vertex<C> c, Vertex<D> d, QuadFunction<A, B, C, D, O> op) {
        super(a, b, c, d);
        this.op = op;
    }

    @Override
    protected O op(A a, B b, C c, D d) {
        return op.apply(a, b, c, d);
    }
}
