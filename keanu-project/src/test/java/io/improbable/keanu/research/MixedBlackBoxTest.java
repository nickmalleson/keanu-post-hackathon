package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesNet;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class MixedBlackBoxTest {

    public static Pair<Integer[], Double[]> model(Integer[] integerInputs,
                                                  Double[] doubleInputs,
                                                  RandomFactory<Double> random) {

        Integer[] integersOut = new Integer[2];
        Double[] doublesOut = new Double[4];

        Integer integerTotal = 0;
        Integer integerProduct = 1;
        for (int input : integerInputs) {
            integerTotal += input;
            integerProduct *= input;
        }
        integersOut[0] = integerTotal;
        integersOut[1] = integerProduct;

        Double doubleTotal = 0.0;
        Double doubleProduct = 1.0;
        for (double input : doubleInputs) {
            doubleTotal += input;
            doubleProduct *= input;
        }
        doublesOut[0] = doubleTotal;
        doublesOut[1] = doubleProduct;

        Double mixedTotal = integerTotal + doubleTotal;
        Double mixedProduct = integerProduct * doubleProduct;
        doublesOut[2] = mixedTotal;
        doublesOut[3] = mixedProduct;

        return new Pair<>(integersOut, doublesOut);
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> doubleInputs = new ArrayList<>(2);
        doubleInputs.add(new GaussianVertex(5.0, 3.0));
        doubleInputs.add(new GaussianVertex(6.0, 3.0));

        ArrayList<IntegerVertex> integerInputs = new ArrayList<>(2);
        integerInputs.add(new PoissonVertex(7));
        integerInputs.add(new PoissonVertex(5));

        MixedInputOutputBlackBox box = new MixedInputOutputBlackBox(integerInputs,
                                                                    doubleInputs,
                                                                    MixedBlackBoxTest::model,
                                                                   2,
                                                                   4);

//        box.fuzzyObserveDoubleOutput(0, 16.0, 1.0);
//        box.fuzzyObserveDoubleOutput(1, 64.0, 1.0);
//
//        box.fuzzyObserveIntegerOutput(0, 16.0, 1.0);
//        box.fuzzyObserveIntegerOutput(1, 64.0, 1.0);

        box.fuzzyObserveDoubleOutput(2, 64.0, 1.0);

        BayesNet testNet = new BayesNet(box.getConnectedGraph());
        ArrayList<Vertex> fromVertices = new ArrayList<>();
        fromVertices.addAll(doubleInputs);
        fromVertices.addAll(integerInputs);

        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, fromVertices, 10000);

        List<Integer> integerOne = testMet.get(integerInputs.get(0)).asList();
        List<Integer> integerTwo = testMet.get(integerInputs.get(1)).asList();
        List<Double> doubleOne = testMet.get(doubleInputs.get(0)).asList();
        List<Double> doubleTwo = testMet.get(doubleInputs.get(1)).asList();

        for (int i=0; i<integerOne.size(); i++) {
            System.out.println(integerOne.get(i) + " " + integerTwo.get(i) + " " + (integerOne.get(i) + integerTwo.get(i)) + " " + (integerOne.get(i) * integerTwo.get(i)) + " || " +
                doubleOne.get(i) + " " + doubleTwo.get(i) + " " + (doubleOne.get(i) + doubleTwo.get(i)) + " " + (doubleOne.get(i) * doubleTwo.get(i))

            );
        }
    }
}
