package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.BiFunction;

public class BlackBox {

    protected final BiFunction<Double[], RandomFactory<Double>, Double[]> model;
    protected final ArrayList<DoubleVertex> doubleInputs;
    protected final ArrayList<DoubleVertex> doubleOutputs;
    protected final VertexBackedRandomFactory random;

    public BlackBox(ArrayList<DoubleVertex> doubleInputs,
                    BiFunction<Double[], RandomFactory<Double>, Double[]> model,
                    Integer expectedNumberOfGaussians,
                    Integer expectedNumberOfUniforms,
                    Integer expectedNumberOfOutputs) {
        this.model = model;
        this.doubleInputs = doubleInputs;
        this.doubleOutputs = new ArrayList<>(expectedNumberOfOutputs);

        Vertex<Double[]> inputVertex = new ReduceVertex<>(doubleInputs, (ArrayList<DoubleVertex> in) -> {
            Double[] out = new Double[doubleInputs.size()];
            for (int i = 0; i< doubleInputs.size(); i++) {
                out[i] = in.get(i);
            }
            return out;
        });

        random = new VertexBackedRandomFactory(expectedNumberOfGaussians, expectedNumberOfUniforms);
        DoubleListLambdaVertex lambdaVertex = new DoubleListLambdaVertex(inputVertex, model, random);

        for (int i=0; i<expectedNumberOfOutputs; i++) {
            doubleOutputs.add(new DoubleArrayIndexingVertex(lambdaVertex, i));
        }
    }

    public BlackBox(ArrayList<DoubleVertex> doubleInputs,
                    BiFunction<Double[], RandomFactory<Double>, Double[]> model,
                    Integer expectedNumberOfOutputs) {
        this(doubleInputs, model,
            expectedNumberOfOutputs*5, expectedNumberOfOutputs*5,
            expectedNumberOfOutputs);
    }

    public GaussianVertex fuzzyObserve(Integer outputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(doubleOutputs.get(outputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public Set<? extends Vertex> getConnectedGraph() {
        Set<Vertex> vertices = doubleOutputs.get(0).getConnectedGraph();
        vertices.addAll(random.listOfGaussians);
        vertices.addAll(random.listOfUniforms);
        return vertices;
    }
}
