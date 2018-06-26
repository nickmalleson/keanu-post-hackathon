package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class DoubleArrayIndexingVertex extends DoubleBinaryOpLambda<Double [], IntegerTensor> {

    public DoubleArrayIndexingVertex(Vertex<Double []> input, Vertex<IntegerTensor> index) {
        super(input, index, (Double [] in, IntegerTensor i) -> DoubleTensor.scalar(in[i.scalar()]));
    }

    public DoubleArrayIndexingVertex(Vertex<Double []> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }
    }
