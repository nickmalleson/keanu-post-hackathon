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
    protected final ArrayList<DoubleVertex> inputs;
    protected final ArrayList<DoubleVertex> outputs;
    protected final VertexBackedRandomFactory random;

    public BlackBox(ArrayList<DoubleVertex> inputs,
                    BiFunction<Double[], RandomFactory<Double>, Double[]> model,
                    Integer expectedNumberOfOutputs) {
        this.model = model;
        this.inputs = inputs;
        this.outputs = new ArrayList<>(expectedNumberOfOutputs);

        Vertex<Double[]> inputVertex = new ReduceVertex<>(inputs, (ArrayList<Double> in) -> {
            Double[] out = new Double[inputs.size()];
            for (int i=0; i<inputs.size(); i++) {
                out[i] = in.get(i);
            }
            return out;
        });

        // TODO this isn't brilliant...
        int numberOfGaussians = 10;
        int numberOfUniforms = 10;

        random = new VertexBackedRandomFactory(numberOfGaussians, numberOfUniforms);
        DoubleListLambdaVertex lambdaVertex = new DoubleListLambdaVertex(inputVertex, model, random);

        for (int i=0; i<expectedNumberOfOutputs; i++) {
            outputs.add(new DoubleArrayIndexingVertex(lambdaVertex, i));
        }
    }
    
    public GaussianVertex fuzzyObserve(Integer outputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(outputs.get(outputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public Set<? extends Vertex> getConnectedGraph() {
        Set<Vertex> vertices = outputs.get(0).getConnectedGraph();
        vertices.addAll(random.listOfGaussians);
        vertices.addAll(random.listOfUniforms);
        return vertices;
    }
}



// how many gaussian calls,
// how many uniform calls