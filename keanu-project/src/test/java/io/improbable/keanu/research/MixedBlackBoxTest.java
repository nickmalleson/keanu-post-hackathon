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

        Integer[] intsOut = new Integer[2];
        Double[] dubsOut = new Double[3];

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
        dubsOut[2] = integerInputs[0] * doubleInputs[0];

        return new Pair<>(intsOut, dubsOut);
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> doubleInputs = new ArrayList<>(2);
        doubleInputs.add(new GaussianVertex(5.5, 3.0));
        doubleInputs.add(new GaussianVertex(6.1, 2.0));

        ArrayList<IntegerVertex> integerInputs = new ArrayList<>(2);
        integerInputs.add(new PoissonVertex(7));
        integerInputs.add(new PoissonVertex(5));

        MixedInputOutputBlackBox box = new MixedInputOutputBlackBox(integerInputs, doubleInputs,
            MixedBlackBoxTest::model, 2, 3);

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

        List<Integer> intOne = testMet.get(integerInputs.get(0)).asList();
        List<Integer> intTwo = testMet.get(integerInputs.get(1)).asList();
        List<Double> dubOne = testMet.get(doubleInputs.get(0)).asList();
        List<Double> dubTwo = testMet.get(doubleInputs.get(1)).asList();

        for (int i=0; i<intOne.size(); i++) {
            System.out.println(intOne.get(i) + " " + intTwo.get(i) + " " + (intOne.get(i) + intTwo.get(i)) + " " + (intOne.get(i) * intTwo.get(i)) + " || " +
                dubOne.get(i) + " " + dubTwo.get(i) + " " + (dubOne.get(i) + dubTwo.get(i)) + " " + (dubOne.get(i) * dubTwo.get(i))

            );
        }
    }
}
