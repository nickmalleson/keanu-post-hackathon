package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;

import java.util.function.BiFunction;

public class IntegerListLambdaVertex extends UnaryOpLambda<Integer[], Integer[]> {

    public IntegerListLambdaVertex(Vertex<Integer[]> arrayOfInputs,
                                   BiFunction<Integer[], RandomFactory<Double>, Integer[]> lambda,
                                   VertexBackedRandomFactory random) {
        super(arrayOfInputs, (Integer[] in) -> lambda.apply(in, random));
    }

}
