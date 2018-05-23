package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import org.apache.commons.math3.util.Pair;


import java.util.ArrayList;
import java.util.Set;

public class MixedInputOutputBlackBox {
    
    private final TriFunction<Integer [], Double [], RandomFactory<Double>, Pair<Integer [], Double []> > model;
    public ArrayList<DoubleVertex> doubleInputs;
    public ArrayList<DoubleVertex> doubleOutputs;
    public ArrayList<IntegerVertex> integerInputs;
    public ArrayList<IntegerVertex> integerOutputs;
    protected final VertexBackedRandomFactory random;


    public MixedInputOutputBlackBox(ArrayList<IntegerVertex> integerInputs,
                                    ArrayList<DoubleVertex> doubleInputs,
                                    TriFunction<Integer [], Double [], RandomFactory<Double>, Pair<Integer [], Double []> > model,
                                    Integer expectedNumberOfIntegersOut,
                                    Integer expectedNumberOfDoublesOut) {
        this.model = model;
        this.integerInputs = integerInputs;
        this.doubleInputs = doubleInputs;
        this.integerOutputs = new ArrayList<>(expectedNumberOfIntegersOut);
        this.doubleOutputs = new ArrayList<>(expectedNumberOfDoublesOut);

        // TODO this isn't brilliant...
        int numberOfGaussians = 10;
        int numberOfUniforms = 10;

        Vertex<Integer[]> integersInputVertex = new ReduceVertex<>(integerInputs, (ArrayList<Integer> in) -> {
            Integer[] out = new Integer[integerInputs.size()];
            for (int i=0; i<integerInputs.size(); i++) { out[i] = in.get(i); }
            return out; });
        Vertex<Double[]> doublesInputVertex = new ReduceVertex<>(doubleInputs, (ArrayList<Double> in) -> {
            Double[] out = new Double[doubleInputs.size()];
            for (int i=0; i<doubleInputs.size(); i++) { out[i] = in.get(i); }
            return out; });

        random = new VertexBackedRandomFactory(numberOfGaussians, numberOfUniforms);
        MixedListLambdaVertex lambdaVertex = new MixedListLambdaVertex(integersInputVertex, doublesInputVertex, model, random);
        PairVertexGetFirst<Integer[], Double[]> integersVertex = new PairVertexGetFirst<>(lambdaVertex);
        PairVertexGetSecond<Integer[], Double[]> doublesVertex = new PairVertexGetSecond<>(lambdaVertex);

        for (int i=0; i<expectedNumberOfIntegersOut; i++) {
            integerOutputs.add(new IntegerArrayIndexingVertex(integersVertex, i));
        }
        for (int i=0; i<expectedNumberOfDoublesOut; i++) {
            doubleOutputs.add(new DoubleArrayIndexingVertex(doublesVertex, i));
        }
    }

    public GaussianVertex fuzzyObserveInput(Integer inputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(doubleOutputs.get(inputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public GaussianVertex fuzzyObserveOutput(Integer outputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(doubleOutputs.get(outputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public Set<? extends Vertex> getConnectedGraph() {
        Set<Vertex> vertices = integerOutputs.get(0).getConnectedGraph();
        vertices.addAll(random.listOfUniforms);
        vertices.addAll(random.listOfGaussians);
        return vertices;
    }
}
