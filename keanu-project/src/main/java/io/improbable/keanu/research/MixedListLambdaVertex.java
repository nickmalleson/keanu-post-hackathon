package io.improbable.keanu.research;

import io.improbable.keanu.research.randomfactory.RandomFactory;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.binary.BinaryOpLambda;
import org.apache.commons.math3.util.Pair;

public class MixedListLambdaVertex extends BinaryOpLambda<IntegerTensor[], DoubleTensor[], Pair<IntegerTensor[], DoubleTensor[]>> {

    public MixedListLambdaVertex(Vertex<IntegerTensor[]> integerInputs,
                                 Vertex<DoubleTensor[]> doubleInputs,
                                 TriFunction<IntegerTensor[], DoubleTensor[], RandomFactory, Pair<IntegerTensor[], DoubleTensor[]>> lambda,
                                 VertexBackedRandomFactory random) {
        super(integerInputs, doubleInputs, (IntegerTensor[] integersIn, DoubleTensor[] doublesIn)
            -> lambda.apply(integersIn, doublesIn, random));
    }
}
