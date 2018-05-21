package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.binary.BinaryOpLambda;

import java.util.ArrayList;

public class MixedListLambdaVertex extends BinaryOpLambda<ArrayList<Integer>, ArrayList<Double>, MixedModelIO> {

    public MixedListLambdaVertex(MixedModelIO inputOutput,
                                 TriFunction<ArrayList<Integer>, ArrayList<Double>, RandomFactory<Double>, MixedModelIO> lambda,
                                 VertexBackedRandomFactory random) {
        super(inputOutput.integersIn, inputOutput.doublesIn, (ArrayList<Integer> integersIn, ArrayList<Double> doublesIn)
            -> lambda.apply(integersIn, doublesIn, random));
    }
}
