package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class IntegerArrayIndexingVertex extends IntegerBinaryOpLambda<Integer[], Integer> {

    public IntegerArrayIndexingVertex(Vertex<Integer[]> input, Vertex<Integer> index) {
        super(input, index, (Integer[] in, Integer i) -> in[i]);
    }

    public IntegerArrayIndexingVertex(Vertex<Integer[]> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }
}
