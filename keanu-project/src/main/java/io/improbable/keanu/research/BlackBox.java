package io.improbable.keanu.research;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.DoubleTensorSplitVertex;
import io.improbable.keanu.research.vertices.ReduceVertex;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class BlackBox {

    protected final BiFunction<DoubleTensor[], RandomGenerator, DoubleTensor[]> model;
    protected final ArrayList<DoubleVertex> doubleInputs;
    public final ArrayList<DoubleVertex> doubleOutputs;
    protected final VertexBackedRandomGenerator random;

    public BlackBox(ArrayList<DoubleVertex> doubleInputs,
                    BiFunction<DoubleTensor[], RandomGenerator, DoubleTensor[]> model,
                    Integer expectedNumberOfDoubleDraws,
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

        random = new VertexBackedRandomGenerator(expectedNumberOfDoubleDraws, 0 , 0);
        DoubleListLambdaVertex lambdaVertex = new DoubleListLambdaVertex(inputVertex, model, random);

        for (int i = 0; i < expectedNumberOfOutputs; i++) {
            doubleOutputs.add(new DoubleTensorSplitVertex(lambdaVertex, i));
        }
    }

    public BlackBox(ArrayList<DoubleVertex> doubleInputs,
                    BiFunction<DoubleTensor[], RandomGenerator, DoubleTensor[]> model,
                    Integer expectedNumberOfOutputs) {
        this(doubleInputs, model,
            expectedNumberOfOutputs*10,
            expectedNumberOfOutputs);
    }

    public GaussianVertex fuzzyObserve(Integer outputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(doubleOutputs.get(outputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public Set<? extends Vertex> getConnectedGraph() {
        Set<Vertex> vertices = doubleOutputs.get(0).getConnectedGraph();
        vertices.addAll(random.getAllVertices());
        return vertices;
    }
}


