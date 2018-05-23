package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesNet;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MixedBlackBoxTest {

    public static Pair<Integer[], Double[]> model(Integer[] integerInputs,
                                                  Double[] doubleInputs,
                                                  RandomFactory<Double> random) {

        Integer[] intsOut = new Integer[2];
        Double[] dubsOut = new Double[2];

        Integer intTotal = 0;
        Integer intProduct = 1;
        for (int input : integerInputs) {
            intTotal += input;
            intProduct *= input;
        }
        intsOut[0] = intTotal;
        intsOut[1] = intProduct;

        Double dubTotal = 0.0;
        Double dubProduct = 1.0;
        for (double input : doubleInputs) {
            dubTotal += input;
            dubProduct *= input;
        }
        dubsOut[0] = dubTotal;
        dubsOut[1] = dubProduct;

        return new Pair<>(intsOut, dubsOut);
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> doubleInputs = new ArrayList<>(2);
        doubleInputs.add(new GaussianVertex(5.5, 3.0));
        doubleInputs.add(new GaussianVertex(6.1, 2.0));

        ArrayList<IntegerVertex> integerInputs = new ArrayList<>(2);
        integerInputs.add(new PoissonVertex(2));
        integerInputs.add(new PoissonVertex(3));

        MixedInputOutputBlackBox box = new MixedInputOutputBlackBox(integerInputs, doubleInputs,
            MixedBlackBoxTest::model, 2, 2);

        box.fuzzyObserveOutput(0, 14.0, 0.5);
        box.integerOutputs.get(0).observe(14);

        BayesNet testNet = new BayesNet(box.getConnectedGraph());
        ArrayList<Vertex> fromVertices = new ArrayList<>();
        fromVertices.addAll(doubleInputs);
        fromVertices.addAll(integerInputs);

        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, fromVertices, 100);

        List<Integer> inOne = testMet.get(integerInputs.get(0)).asList();
        List<Integer> inTwo = testMet.get(integerInputs.get(1)).asList();

        for (int i=0; i<inOne.size(); i++) {
            System.out.println(inOne.get(i) + " " + inTwo.get(i) + (inOne.get(i) + inTwo.get(i)));
        }


    }


}
