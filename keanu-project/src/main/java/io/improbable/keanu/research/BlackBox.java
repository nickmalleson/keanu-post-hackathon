package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class BlackBox {

    protected final BiFunction<DoubleTensor[], RandomFactory<Double>, DoubleTensor[]> model;
    protected final ArrayList<DoubleVertex> doubleInputs;
    public final ArrayList<DoubleVertex> doubleOutputs;
    protected final VertexBackedRandomFactory random;

    public BlackBox(ArrayList<DoubleVertex> doubleInputs,
                    BiFunction<DoubleTensor[], RandomFactory<Double>, DoubleTensor[]> model,
                    Integer expectedNumberOfGaussians,
                    Integer expectedNumberOfUniforms,
                    Integer expectedNumberOfOutputs) {
        this.model = model;
        this.doubleInputs = doubleInputs;
        this.doubleOutputs = new ArrayList<>(expectedNumberOfOutputs);

        Vertex<DoubleTensor[]> inputVertex = new ReduceVertex<>(doubleInputs, (List<DoubleTensor> in) -> {
            DoubleTensor[] out = new DoubleTensor[doubleInputs.size()];
            for (int i = 0; i < doubleInputs.size(); i++) {
                out[i] = in.get(i);
            }
            return out;
        });

        random = new VertexBackedRandomFactory(expectedNumberOfGaussians, expectedNumberOfUniforms);
        DoubleListLambdaVertex lambdaVertex = new DoubleListLambdaVertex(inputVertex, model, random);

        for (int i = 0; i < expectedNumberOfOutputs; i++) {
            doubleOutputs.add(new DoubleArrayIndexingVertex(lambdaVertex, i));
        }

        System.out.println("BlackBox output1: " + doubleOutputs.get(0).getValue().scalar());
    }

    public BlackBox(ArrayList<DoubleVertex> doubleInputs,
                    BiFunction<DoubleTensor[], RandomFactory<Double>, DoubleTensor[]> model,
                    Integer expectedNumberOfOutputs) {
        this(doubleInputs, model,
            expectedNumberOfOutputs * 5, expectedNumberOfOutputs * 5,
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

        System.out.println("BlackBox.getConnectedGraph(): BN vertices = " + vertices.size() +
            ", random.listOfGaussians = " + random.listOfGaussians.size() + ", random.listOfUniforms = " + random.listOfUniforms.size());
        return vertices;
    }
}


