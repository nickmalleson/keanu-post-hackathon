package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;

import java.util.function.BiFunction;

public class DoubleListLambdaVertex extends UnaryOpLambda<Double[], Double[]> {

    public DoubleListLambdaVertex(int numberOfGaussians, int numberOfUniforms,
                                  Vertex<Double[]> arrayOfInputs,
                                  BiFunction<Double[], RandomFactory<Double>, Double[]> lambda) {
        super(arrayOfInputs, (Double[] in) -> {
            return lambda.apply(in, new VertexBackedRandomFactory(numberOfGaussians, numberOfUniforms));
        });
    }
}
