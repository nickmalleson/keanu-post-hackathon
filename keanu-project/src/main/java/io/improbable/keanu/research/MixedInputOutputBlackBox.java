package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;

import java.util.ArrayList;

public class MixedInputOutputBlackBox {
    
    protected final TriFunction<ArrayList<Integer>, ArrayList<Double>, RandomFactory<Double>, MixedModelIO> model;
    protected final MixedModelIO io;
    protected final VertexBackedRandomFactory random;

    public MixedInputOutputBlackBox(MixedModelIO io,
                                    TriFunction<ArrayList<Integer>, ArrayList<Double>, RandomFactory<Double>, MixedModelIO> model) {
        this.model = model;
        this.io = io;

        // TODO this isn't brilliant...
        int numberOfGaussians = 10;
        int numberOfUniforms = 10;
        
        random = new VertexBackedRandomFactory(numberOfGaussians, numberOfUniforms);
        
        MixedListLambdaVertex lambdaVertex = new MixedListLambdaVertex(io, model, random);

        for (int i=0; i<io.expectedNumberOfDoublesOut; i++) {
            io.listOfDoubleVertexOutputs.add(new DoubleArrayIndexingVertex(lambdaVertex.getValue().doublesOut, i));
        }

        for (int i=0; i<io.expectedNumberOfIntegersOut; i++) {
            io.listOfIntegerVertexOutputs.add(new IntegerArrayIndexingVertex(lambdaVertex.getValue().integersOut, 0));
        }
    }
}
