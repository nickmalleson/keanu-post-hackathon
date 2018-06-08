//package io.improbable.keanu.research;
//
//import io.improbable.keanu.algorithms.NetworkSamples;
//import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
//import io.improbable.keanu.network.BayesNet;
//import io.improbable.keanu.randomfactory.RandomFactory;
//import io.improbable.keanu.vertices.Vertex;
//import io.improbable.keanu.vertices.dbl.DoubleVertex;
//import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class DoubleListLambdaVertexTest {
//
//    public static Double[] model(Double[] inputs, RandomFactory<Double> random) {
//        Double[] output = new Double[2];
//        output[0] = inputs[0] * inputs[1];
//        output[1] = inputs[0] + inputs[1];
//        return output;
//    }
//
//    public static void main (String[] args) {
//        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
//        inputs.add(new GaussianVertex(5.5, 3.0));
//        inputs.add(new GaussianVertex(6.1, 2.0));
//
//        Vertex<Double[]> inputVertex = new ReduceVertex<>(inputs, (ArrayList<Double> in) -> {
//            Double[] out = new Double[inputs.size()];
//            out[0] = in.get(0);
//            out[1] = in.get(1);
//            return out; });
//
//        VertexBackedRandomFactory random = new VertexBackedRandomFactory(0, 1);
//        DoubleListLambdaVertex vert = new DoubleListLambdaVertex(inputVertex,  DoubleListLambdaVertexTest::model, random);
//
//        DoubleArrayIndexingVertex outputOne = new DoubleArrayIndexingVertex(vert, 0);
//        DoubleArrayIndexingVertex outputTwo = new DoubleArrayIndexingVertex(vert, 1);
//
//        GaussianVertex observedOutput = new GaussianVertex(outputTwo, 0.5);
//        observedOutput.observe(15.0);
//
//        BayesNet testNet = new BayesNet(vert.getConnectedGraph());
//        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, inputs, 100);
//
//        List<Double> inOne = testMet.get(inputs.get(0)).asList();
//        List<Double> inTwo = testMet.get(inputs.get(1)).asList();
//
//        for (int i=0; i<inOne.size(); i++) {
//            System.out.println(inOne.get(i) + " " + inTwo.get(i) + " " + (inOne.get(i) + inTwo.get(i)));
//        }
//    }
//}
