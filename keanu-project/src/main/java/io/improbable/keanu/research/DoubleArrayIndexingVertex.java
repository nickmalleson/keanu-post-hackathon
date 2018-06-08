package io.improbable.keanu.research;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DoubleBinaryOpLambda;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class DoubleArrayIndexingVertex extends DoubleBinaryOpLambda<DoubleTensor[], IntegerTensor> {

    public DoubleArrayIndexingVertex(Vertex<DoubleTensor[]> input, IntegerVertex index) {
        super(input, index, (DoubleTensor[] in, IntegerTensor i) -> in[i.scalar()]);
    }

    public DoubleArrayIndexingVertex(Vertex<DoubleTensor[]> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }
}
