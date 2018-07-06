package io.improbable.keanu.research;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiFunction;

public class IntegerListLambdaVertex extends UnaryOpLambda<IntegerTensor[], IntegerTensor[]> {

    public IntegerListLambdaVertex(Vertex<IntegerTensor[]> arrayOfInputs,
                                   BiFunction<IntegerTensor[], RandomGenerator, IntegerTensor[]> lambda,
                                   VertexBackedRandomGenerator random) {
        super(arrayOfInputs, (IntegerTensor[] in) -> lambda.apply(in, random));
    }

}
