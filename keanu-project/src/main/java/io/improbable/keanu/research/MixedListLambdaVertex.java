package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.binary.BinaryOpLambda;
import org.apache.commons.math3.util.Pair;

public class MixedListLambdaVertex extends BinaryOpLambda<Integer[], Double[], Pair<Integer [], Double []>> {

    public MixedListLambdaVertex(Vertex<Integer[]> integerInputs,
                                 Vertex<Double[]> doubleInputs,
                                 TriFunction<Integer [], Double [], RandomFactory<Double>, Pair<Integer [], Double []>> lambda,
                                 VertexBackedRandomFactory random) {
        super(integerInputs, doubleInputs, (Integer[] integersIn, Double[] doublesIn)
            -> lambda.apply(integersIn, doublesIn, random));
    }
}
