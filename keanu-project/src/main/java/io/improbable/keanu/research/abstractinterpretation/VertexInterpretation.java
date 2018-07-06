package io.improbable.keanu.research.abstractinterpretation;

import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.nonprobabilistic.ConstantBoolVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

public class VertexInterpretation implements AbstractInterpretation<DoubleVertex, IntegerVertex, BoolVertex> {
    @Override
    public DoubleVertex newDouble(double x) {
        return new ConstantDoubleVertex(x);
    }

    @Override
    public IntegerVertex newInt(int i) {
        return new ConstantIntegerVertex(i);
    }

    @Override
    public BoolVertex newBool(boolean b) {
        return new ConstantBoolVertex(b);
    }
}
