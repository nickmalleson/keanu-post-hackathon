package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.VertexSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.dbl.ScalarDoubleTensor;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.vis.Vizer;

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

    public void test (Double desiredSum, Double desiredProduct, Double observationUncertainty) {

        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
        inputs.add(new GaussianVertex(desiredSum/2.0, desiredSum/3.0));
        inputs.add(new GaussianVertex(desiredSum/2.0, desiredSum/3.0));

        BlackBox box = new BlackBox(inputs, BlackBoxTest::model, 2);

//        GaussianVertex g = new DoubleTensor()
    }

    public static void main (String[] args) {
        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
        inputs.add(new GaussianVertex(5.0, 3.0));
        inputs.add(new GaussianVertex(6.0, 3.0));

        BlackBox box = new BlackBox(inputs, BlackBoxTest::model, 2);

        box.fuzzyObserve(0, 49.0, 0.5);
        box.fuzzyObserve(1, 14.0, 0.5);

        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        NonGradientOptimizer daveTest = new NonGradientOptimizer(testNet);
        daveTest.maxAPosteriori(100000, 14.0);

        System.out.println("Input 0: " + inputs.get(0).getValue().scalar() + " Input 1: " + inputs.get(1).getValue().scalar());
        Double MAPInputOne = inputs.get(0).getValue().scalar();
        Double MAPInputTwo = inputs.get(1).getValue().scalar();
        System.out.println("MAP Error One: " + (MAPInputOne*MAPInputTwo-49.0) + " MAP Error Two: " + (MAPInputOne+MAPInputTwo-14.0));

        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, inputs, 1000000).drop(10000).downSample(100);

        Double answer = testMet.probability( sample -> {
            Double input0 = sample.get(inputs.get(0)).scalar();
            Double input1 = sample.get(inputs.get(1)).scalar();
            Double sum = input0 + input1;
            Double product = input0 * input1;
            return (13.5 < sum && sum < 14.5 && 48.5 < product && product < 49.5);
        } );

        VertexSamples<ScalarDoubleTensor> samples0 = testMet.get(inputs.get(0).getId());
        VertexSamples<ScalarDoubleTensor> samples1 = testMet.get(inputs.get(1).getId());

        ArrayList<Double> test = new ArrayList<>();
        Double Onetotal = 0.0;
        Double Zerototal = 0.0;
        for (int i=0; i<samples1.asList().size(); i++) {
            test.add(samples1.asList().get(i).scalar() + samples0.asList().get(i).scalar());
            Onetotal += samples1.asList().get(i).scalar();
            Zerototal += samples0.asList().get(i).scalar();
        }
        Double OneMean = Onetotal/samples1.asList().size();
        Double TwoMean = Zerototal/samples1.asList().size();

        System.out.println("Input One Mean: " + OneMean + " Input Two Mean: " + TwoMean);
        System.out.println("Error One: " + (OneMean*TwoMean-49.0) + " Error Two: " + (TwoMean+OneMean-14.0));


        System.out.println(answer);

        Vizer.histogram(test);
    }
}

// Pass in x and ys, pass out m and c and error, fuzzy observe error to be 0
