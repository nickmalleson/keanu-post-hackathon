package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class IntegerTensorSplitVertex extends IntegerBinaryOpLambda<IntegerTensor[], IntegerTensor> {

    public IntegerTensorSplitVertex(Vertex<IntegerTensor[]> input, IntegerVertex index) {
        super(input, index, (IntegerTensor[] in, IntegerTensor i) -> in[i.scalar()]);
    }

    public IntegerTensorSplitVertex(Vertex<IntegerTensor[]> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }
}
