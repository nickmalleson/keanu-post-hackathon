package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class IntegerArrayIndexingVertex extends IntegerBinaryOpLambda<Integer [], IntegerTensor> {

    public IntegerArrayIndexingVertex(Vertex<Integer []> input, Vertex<IntegerTensor> index) {
        super(input, index, (Integer [] in, IntegerTensor i) -> IntegerTensor.scalar(in[i.scalar()]));
    }

    public IntegerArrayIndexingVertex(Vertex<Integer []> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }


}
