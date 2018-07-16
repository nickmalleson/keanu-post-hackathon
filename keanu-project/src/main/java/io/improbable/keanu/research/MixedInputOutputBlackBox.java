package io.improbable.keanu.research;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.*;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixedInputOutputBlackBox {
    
    public ArrayList<DoubleVertex> doubleInputs;
    public ArrayList<DoubleVertex> doubleOutputs;
    public ArrayList<IntegerVertex> integerInputs;
    public ArrayList<IntegerVertex> integerOutputs;
    protected final VertexBackedRandomGenerator random;

    public MixedInputOutputBlackBox(ArrayList<IntegerVertex> integerInputs,
                                    ArrayList<DoubleVertex> doubleInputs,
                                    TriFunction<IntegerTensor[], DoubleTensor[], RandomGenerator, Pair<IntegerTensor[], DoubleTensor[]> > model,
                                    Integer expectedNumberOfDoubleDraws,
                                    Integer expectedNumberOfIntegerDraws,
                                    Integer expectedNumberOfIntegersOut,
                                    Integer expectedNumberOfDoublesOut) {

        this.integerInputs = integerInputs;
        this.doubleInputs = doubleInputs;
        this.integerOutputs = new ArrayList<>(expectedNumberOfIntegersOut);
        this.doubleOutputs = new ArrayList<>(expectedNumberOfDoublesOut);

        Vertex<IntegerTensor[]> integersInputVertex = new ReduceVertex<>(integerInputs, (List<IntegerTensor> in) -> {
            IntegerTensor[] out = new IntegerTensor[integerInputs.size()];
            for (int i=0; i<integerInputs.size(); i++) { out[i] = in.get(i); }
            return out; });
        Vertex<DoubleTensor[]> doublesInputVertex = new ReduceVertex<>(doubleInputs, (List<DoubleTensor> in) -> {
            DoubleTensor[] out = new DoubleTensor[doubleInputs.size()];
            for (int i=0; i<doubleInputs.size(); i++) { out[i] = in.get(i); }
            return out; });

        random = new VertexBackedRandomGenerator(expectedNumberOfDoubleDraws, expectedNumberOfIntegerDraws, 0);
        MixedListLambdaVertex lambdaVertex = new MixedListLambdaVertex(integersInputVertex, doublesInputVertex, model, random);
        PairGetFirstVertex<IntegerTensor[], DoubleTensor[]> integersVertex = new PairGetFirstVertex<>(lambdaVertex);
        PairGetSecondVertex<IntegerTensor[], DoubleTensor[]> doublesVertex = new PairGetSecondVertex<>(lambdaVertex);

        for (int i=0; i<expectedNumberOfIntegersOut; i++) {
            integerOutputs.add(new IntegerTensorSplitVertex(integersVertex, i));
        }
        for (int i=0; i<expectedNumberOfDoublesOut; i++) {
            doubleOutputs.add(new DoubleTensorSplitVertex(doublesVertex, i));
        }
    }

    public MixedInputOutputBlackBox(ArrayList<IntegerVertex> integerInputs,
                                    ArrayList<DoubleVertex> doubleInputs,
                                    TriFunction<IntegerTensor[], DoubleTensor[], RandomGenerator,
                                                Pair<IntegerTensor[], DoubleTensor[]>> model,
                                    Integer expectedNumberOfIntegersOut,
                                    Integer expectedNumberOfDoublesOut) {
        this(integerInputs, doubleInputs, model,
            (expectedNumberOfDoublesOut+expectedNumberOfIntegersOut)*5,
            (expectedNumberOfDoublesOut+expectedNumberOfIntegersOut)*5,
            expectedNumberOfIntegersOut, expectedNumberOfDoublesOut);
    }

    public GaussianVertex fuzzyObserveDoubleInput(Integer inputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(doubleInputs.get(inputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public GaussianVertex fuzzyObserveDoubleOutput(Integer outputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(doubleOutputs.get(outputIndex), error);
        vertex.observe(observation);
        return vertex;
    }

    public GaussianVertex fuzzyObserveIntegerInput(Integer inputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(new CastDoubleVertex(integerInputs.get(inputIndex)), error);
        vertex.observe(observation);
        return vertex;
    }

    public GaussianVertex fuzzyObserveIntegerOutput(Integer outputIndex, Double observation, Double error) {
        GaussianVertex vertex = new GaussianVertex(new CastDoubleVertex(integerOutputs.get(outputIndex)), error);
        vertex.observe(observation);
        return vertex;
    }

    public Set<? extends Vertex> getConnectedGraph() {
        Set<Vertex> vertices = integerOutputs.get(0).getConnectedGraph();
        vertices.addAll(random.getAllVertices());
        return vertices;
    }
}
