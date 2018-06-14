package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;

import java.util.function.BiFunction;

public class IntegerListLambdaVertex extends UnaryOpLambda<IntegerTensor[], IntegerTensor[]> {

    public IntegerListLambdaVertex(Vertex<IntegerTensor[]> arrayOfInputs,
                                   BiFunction<IntegerTensor[], io.improbable.keanu.research.randomfactory.RandomFactory, IntegerTensor[]> lambda,
                                   VertexBackedRandomFactory random) {
        super(arrayOfInputs, (IntegerTensor[] in) -> lambda.apply(in, random));
    }

}
