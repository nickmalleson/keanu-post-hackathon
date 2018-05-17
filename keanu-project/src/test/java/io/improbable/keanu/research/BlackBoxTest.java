package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesNet;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

import java.util.ArrayList;
import java.util.List;

public class BlackBoxTest {

    public static Double[] model(Double[] inputs, RandomFactory<Double> random) {
        Double[] output = new Double[2];
        output[0] = inputs[0] * inputs[1];
        output[1] = inputs[0] + inputs[1];
        return output;
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
        inputs.add(new GaussianVertex(5.5, 3.0));
        inputs.add(new GaussianVertex(6.1, 2.0));

        BlackBox box = new BlackBox(inputs, BlackBoxTest::model, 2);

        box.fuzzyObserve(1, 14.0, 0.5);

        BayesNet testNet = new BayesNet(box.getConnectedGraph());

        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, inputs, 100);

        List<Double> inOne = testMet.get(inputs.get(0)).asList();
        List<Double> inTwo = testMet.get(inputs.get(1)).asList();

        for (int i=0; i<inOne.size(); i++) {
            System.out.println(inOne.get(i) + " " + inTwo.get(i) + " " + (inOne.get(i) + inTwo.get(i)));
        }

    }
}
