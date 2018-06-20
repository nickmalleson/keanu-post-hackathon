package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;

public class MixedBlackBoxTest {

    public static Pair<IntegerTensor[], DoubleTensor[]> model(IntegerTensor[] integerInputs,
                                                  DoubleTensor[] doubleInputs,
                                                  RandomFactory random) {

        IntegerTensor[] integersOut = new IntegerTensor[2];
        DoubleTensor[] doublesOut = new DoubleTensor[4];

        Integer integerTotal = 0;
        Integer integerProduct = 1;
        for (IntegerTensor input : integerInputs) {
            integerTotal += input.scalar();
            integerProduct *= input.scalar();
        }
        integersOut[0] = IntegerTensor.create(integerTotal, new int[0]);
        integersOut[1] = IntegerTensor.create(integerProduct, new int[0]);

        Double doubleTotal = 0.0;
        Double doubleProduct = 1.0;
        for (DoubleTensor input : doubleInputs) {
            doubleTotal += input.scalar();
            doubleProduct *= input.scalar();
        }
        doublesOut[0] = DoubleTensor.create(doubleTotal, new int[0]);
        doublesOut[1] = DoubleTensor.create(doubleProduct, new int[0]);

        Double mixedTotal = integerTotal + doubleTotal;
        Double mixedProduct = integerProduct * doubleProduct;
        doublesOut[2] = DoubleTensor.create(mixedTotal, new int[0]);
        doublesOut[3] = DoubleTensor.create(mixedProduct, new int[0]);

        return new Pair<>(integersOut, doublesOut);
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> doubleInputs = new ArrayList<>(2);
        doubleInputs.add(new GaussianVertex(5.0, 3.0));
        doubleInputs.add(new GaussianVertex(6.0, 3.0));

        ArrayList<IntegerVertex> integerInputs = new ArrayList<>(2);
        integerInputs.add(new PoissonVertex(4));
        integerInputs.add(new PoissonVertex(5));

        MixedInputOutputBlackBox box = new MixedInputOutputBlackBox(integerInputs,
                                                                    doubleInputs,
                                                                    MixedBlackBoxTest::model,
                                                                   2,
                                                                   4);

        // TODO inputs = [[3, 4], [5.55, 6.66]] give outputs = ([7.0, 12.0], [12.21, 36.963, 19.21, 443.556])

        box.fuzzyObserveIntegerOutput(0, 7.0, 1.0);
        box.fuzzyObserveIntegerOutput(1, 12.0, 1.0);

        box.fuzzyObserveDoubleOutput(0, 12.21, 1.0);
        box.fuzzyObserveDoubleOutput(1, 36.963, 1.0);
        box.fuzzyObserveDoubleOutput(2, 19.21, 1.0);
        box.fuzzyObserveDoubleOutput(3, 443.556, 1.0);

        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());
        ArrayList<Vertex> fromVertices = new ArrayList<>();
        fromVertices.addAll(doubleInputs);
        fromVertices.addAll(integerInputs);

        NonGradientOptimizer optimizer = new NonGradientOptimizer(testNet);
        optimizer.maxAPosteriori(10000, 10.0);
        System.out.println(doubleInputs.get(0).getValue().scalar());

        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, fromVertices, 100000).drop(1000);

        Double answer_doubleOne = testMet.probability( sample -> sample.get(doubleInputs.get(0)).scalar() > 5.0 && sample.get(doubleInputs.get(0)).scalar() < 7.0 );

        System.out.println(answer_doubleOne);


//        List<Integer> integerOne = testMet.get(integerInputs.get(0)).asList();
//        List<Integer> integerTwo = testMet.get(integerInputs.get(1)).asList();
//        List<Double> doubleOne = testMet.get(doubleInputs.get(0)).asList();
//        List<Double> doubleTwo = testMet.get(doubleInputs.get(1)).asList();
//
//        for (int i=0; i<integerOne.size(); i++) {
//            System.out.println(integerOne.get(i) + " " + integerTwo.get(i) + " " + (integerOne.get(i) + integerTwo.get(i)) + " " + (integerOne.get(i) * integerTwo.get(i)) + " || " +
//                doubleOne.get(i) + " " + doubleTwo.get(i) + " " + (doubleOne.get(i) + doubleTwo.get(i)) + " " + (doubleOne.get(i) * doubleTwo.get(i))
//
//            );
//        }
    }
}
