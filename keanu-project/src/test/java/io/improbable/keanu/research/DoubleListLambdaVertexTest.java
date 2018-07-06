package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
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

public class DoubleListLambdaVertexTest {

    public static DoubleTensor[] model(DoubleTensor[] inputs, RandomGenerator random) {
        DoubleTensor[] output = new DoubleTensor[2];
        output[0] = DoubleTensor.create(inputs[0].scalar() * inputs[1].scalar(), new int[0]);
        output[1] = DoubleTensor.create(inputs[0].scalar() + inputs[1].scalar(), new int[0]);
        return output;
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
        inputs.add(new GaussianVertex(5.5, 3.0));
        inputs.add(new GaussianVertex(6.1, 2.0));

        Vertex<DoubleTensor[]> inputVertex = new ReduceVertex<>(inputs, (List<DoubleTensor> in) -> {
            DoubleTensor[] out = new DoubleTensor[inputs.size()];
            out[0] = in.get(0);
            out[1] = in.get(1);
            return out; });

        VertexBackedRandomGenerator random = new VertexBackedRandomGenerator(0, 0,0);
        DoubleListLambdaVertex vert = new DoubleListLambdaVertex(inputVertex,  DoubleListLambdaVertexTest::model, random);

        DoubleTensorSplitVertex outputOne = new DoubleTensorSplitVertex(vert, 0);
        DoubleTensorSplitVertex outputTwo = new DoubleTensorSplitVertex(vert, 1);

        GaussianVertex observedOutput = new GaussianVertex(outputTwo, 0.5);
        observedOutput.observe(15.0);

        BayesianNetwork testNet = new BayesianNetwork(vert.getConnectedGraph());
        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, inputs, 100);

        // TODO add actual test
    }
}
