package StationSim;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.RandomFactory;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class WrapperTest {

    private static int iterations = 100;

    public static Double[] model(RandomFactory rand) {
        Double[] results = new Double[iterations];

        for(int i = 0; i < iterations; i++) {
            results[i] = rand.nextDouble() * rand.nextDouble();
        }
        return results;
    }


    public static void main(String[] args) {


        System.out.println("Starting. Number of iterations: " + iterations);

        // Make truth data
        System.out.println("Making truth data");
        VertexBackedRandomFactory truthRandom = new VertexBackedRandomFactory(iterations * 2, 0, 0);
        Double[] truth = WrapperTest.model(truthRandom);

        System.out.println("Initialising random number stream");
        Wrapper wrap = new Wrapper();
        //VertexBackedRandomFactory random = new VertexBackedRandomFactory(numInputs,, 0, 0);
        RandomFactoryVertex random = new RandomFactoryVertex(iterations * 2, 0, 0);

        ArrayList<DoubleVertex> inputs = new ArrayList<>(0);


        // This is the 'black box' vertex that runs the model. It's input is the random numbers and
        // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
        //BlackBox box = new BlackBox(inputs, wrap::run, Wrapper.numTimeSteps);
        //UnaryOpVertex<RandomFactory,Integer[]> box = new Unar<>( random, wrap::run )
        System.out.println("Initialising black box model");
        UnaryOpLambda<VertexBackedRandomFactory, Integer[]> box = new UnaryOpLambda<>(random, Wrapper::run);

        // This is the list of random numbers that are fed into model (similar to drawing from a distribution,
        // but they're pre-defined in randSource)
        //List<GaussianVertex> randSource  = random.getValue().randDoubleSource;


        // Observe the truth data plus some noise?
        if (OBSERVE) {
            System.out.println("Observing truth data. Adding noise with standard dev: " + sigmaNoise);
            for (Integer i = 0; i < numTimeSteps; i++) {
                // output is the ith element of the model output (from box)
                IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
                // output with a bit of noise. Lower sigma makes it more constrained.
                GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), sigmaNoise);
                // Observe the output
                noisyOutput.observe(truth[i].doubleValue()); //.toDouble().scalar());
            }
        } else {
            System.out.println("Not observing truth data");
        }

        System.out.println("Creating BayesNet");
        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        // Workaround for too many evaluations during sample startup
        random.setAndCascade(random.getValue());

        // Sample: feed each randomNumber in and run the model
        System.out.println("Sampling");
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(testNet, Arrays.asList(box), numSamples);

        // Interrogate the samples

        // Get the number of people per iteration (an array of IntegerTensors) for each sample
        List<Integer[]> samples = sampler.drop(dropSamples).downSample(downSample).get(box).asList();

        // Print Number of people at each iteration in every sample

        writeResults(samples, truth);
        for (int i = 0; i < samples.size(); i++) {
            System.out.print("Sample " + i + ", ");

            Integer[] peoplePerIter = samples.get(i);

            for (int j = 0; j < peoplePerIter.length; j++) {
                System.out.print(peoplePerIter[j] + ",");
            }

            System.out.println("");
        }
    }
}

