package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

import java.util.ArrayList;

public class BlackBoxTest {

    public static DoubleTensor[] model(DoubleTensor[] inputs, RandomFactory<Double> random) {
        DoubleTensor[] output = new DoubleTensor[2];

        double[] output0 = new double[1];
        output0[0] = inputs[0].scalar() * inputs[1].scalar();
        output[0] = DoubleTensor.create(output0);

        double[] output1 = new double[1];
        output1[0] = inputs[0].scalar() + inputs[1].scalar();
        output[1] = DoubleTensor.create(output1);

        return output;
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
        inputs.add(new GaussianVertex(5.0, 3.0));
        inputs.add(new GaussianVertex(6.0, 3.0));

        BlackBox box = new BlackBox(inputs, BlackBoxTest::model, 2);

        box.fuzzyObserve(0, 49.0, 0.5);
        box.fuzzyObserve(1, 14.0, 0.5);

        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        // TODO convert into test, given observations, both inputs should converge to ~ 7.0
        // TODO Maybe test observation of the inputs gives convergence to expected outputs

        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, inputs, 500000).drop(1000);

        Double answer = testMet.probability( sample -> sample.get(inputs.get(0)).scalar() + sample.get(inputs.get(1)).scalar() > 14.0 );
        System.out.println(answer);
    }
}
