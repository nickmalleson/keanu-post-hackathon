package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

public abstract class QuadOpVertex<A, B, C, D, O> extends NonProbabilistic<O> {

    protected final Vertex<A> a;
    protected final Vertex<B> b;
    protected final Vertex<C> c;
    protected final Vertex<D> d;

    public QuadOpVertex(Vertex<A> a, Vertex<B> b, Vertex<C> c, Vertex<D> d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        setParents(a, b, c, d);
    }

    @Override
    public O sample() {
        return op(a.sample(), b.sample(), c.sample(), d.sample());
    }

    public O getDerivedValue() {
        return op(a.getValue(), b.getValue(), c.getValue(), d.getValue());
    }

    protected abstract O op(A a, B b, C c, D d);
}
