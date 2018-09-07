package SimpleModel;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SimpleWrapper {

    /* Model parameters */
    private static final double threshold = 0.5;
    public static final int NUM_RAND_DOUBLES = 2;
    private static final int NUM_ITER = 100;

    /* Hyperparameters */
    private static final double SIGMA_NOISE = 0.1;
    private static final int NUM_SAMPLES = 10000;
    private static final int DROP_SAMPLES = 1;
    //private static final int DROP_SAMPLES = NUM_SAMPLES/4;
    private static final int DOWN_SAMPLE = 1;

    private static final int numObservations = 5; // Number of points to observe (temporary - will be replaced with proper tests)

    private static ArrayList<SimpleModel> models = new ArrayList<>(); // Keep all the models for analysis later

    //private static final long SEED = 1l;
    //private static final RandomGenerator random = new MersenneTwister(SEED);



    /* Admin parameters */
    private static String dirName = "results/simple/"; // Place to store results


    /** Run the SimpleModel and return the count at each iteration **/

    public static Integer[] runModel(RandomGenerator rand) {
        SimpleModel s = new SimpleModel(SimpleWrapper.threshold, rand);

        for (int i=0; i<NUM_ITER; i++) {
            s.step();
        }
        SimpleWrapper.models.add(s);
        return s.getHistory();
    }


    /**
     * Run the probabilistic model
     **/
    public static void run() {
        System.out.println("Starting. Number of iterations: " + NUM_ITER);

        System.out.println("Making truth data");
        System.out.println("Initialising random number stream for truth data");
        VertexBackedRandomGenerator truthRandom = new VertexBackedRandomGenerator(NUM_RAND_DOUBLES, 0, 0);
        Integer[] truth = SimpleWrapper.runModel(truthRandom);

        System.out.println("Truth data length: " + truth.length);
        System.out.println(Arrays.asList(truth).toString() + "\n\n");

        System.out.println("Initialising new random number stream");
        RandomFactoryVertex random = new RandomFactoryVertex(NUM_RAND_DOUBLES, 0, 0);

        // This is the 'black box' vertex that runs the model. It's input is the random numbers and
        // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
        System.out.println("Initialising black box model");
        UnaryOpLambda<VertexBackedRandomGenerator, Integer[]> box =
            new UnaryOpLambda<>( random, SimpleWrapper::runModel);

        // Observe the truth data plus some noise?
        System.out.println("Observing truth data. Adding noise with standard dev: " + SIGMA_NOISE);
        System.out.print("Observing at iterations: ");
        for (Integer i = 0; i < NUM_ITER; i+=NUM_ITER/numObservations) {
            System.out.print(i+",");
            // output is the ith element of the model output (from box)
            IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
            // output with a bit of noise. Lower sigma makes it more constrained.
            GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), SIGMA_NOISE);
            // Observe the output
            noisyOutput.observe(truth[i].doubleValue()); //.toDouble().scalar());
        }
        System.out.println();

        // Create the BayesNet
        System.out.println("Creating BayesNet");
        BayesianNetwork net = new BayesianNetwork(box.getConnectedGraph());
        SimpleWrapper.writeBaysNetToFile(net);

        // Workaround for too many evaluations during sample startup
        //random.setAndCascade(random.getValue());

        // Sample from the posterior
        System.out.println("Sampling");
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(
            net,                        // The bayes net with latent variables (the random numbers?)
            Arrays.asList(box),         // The vertices to include in the returned samples
            NUM_SAMPLES);               // The number of samples

        System.out.println("Finished running MCMC.");

        // Get the number of people per iteration for each sample
        List<Integer[]> peopleSamples = sampler.drop(DROP_SAMPLES).downSample(DOWN_SAMPLE).get(box).asList();
        System.out.println("Have saved " + peopleSamples.size() + " samples and ran " + models.size() + " models");
        SimpleWrapper.writeResults(peopleSamples , truth);

        // Get the random numbers used in each sample

        System.out.println(random.getId());
        List<VertexBackedRandomGenerator> randomParamSamples = sampler.drop(DROP_SAMPLES).downSample(DOWN_SAMPLE).get(random).asList();

    }





    private static void writeBaysNetToFile(BayesianNetwork net) {
        try {
            System.out.println("Writing out graph");
            Writer graphWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Graph_" + System.currentTimeMillis() + ".dot"),
                "utf-8"));
            graphWriter.write(GraphvizKt.toGraphvizString(net, new HashMap<>()) );
            graphWriter.close();
        } catch (IOException ex) {
            System.out.println("Error writing graph to file");
        }
    }

    private static void writeResults(List<Integer[]> samples, Integer[] truth) {

        // Write out random numbers used and the actual results
        Writer w1, w2;
        long time = System.currentTimeMillis();
        try {
            w1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Results_" + time + ".csv"), "utf-8"));

            // Do the truth data first
            for (int val : truth) {
                w1.write(val + ","); // Values
            }
            w1.write("\n");

            // Now the samples.
            for (Integer[] sample : samples) {
                for (int val : sample) {
                    w1.write(val + ",");
                }
                w1.write("\n");
            }
            w1.close();
        } catch (IOException ex) {
            System.out.println("Error writing to file");
        }
    }

    public static void main (String[] args) {

        SimpleWrapper.run();
    }
}
