package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.binary.BinaryOpLambda;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class DisaggregateVertex<T> extends BinaryOpLambda<T[], Integer, T> {

    public DisaggregateVertex(Vertex<T[]> input, Vertex<Integer> index) {
        super(input, index, (T[] in, Integer i) -> in[i]
        );
    }

    public DisaggregateVertex(Vertex<T[]> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }
}
