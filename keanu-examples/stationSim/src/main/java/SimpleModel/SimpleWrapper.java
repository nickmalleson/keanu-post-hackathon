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
    public static final int NUM_RAND_DOUBLES = 10;
    private static final int NUM_ITER = 1500;

    /* Hyperparameters */
    private static final double SIGMA_NOISE = 0.1;
    private static final int NUM_SAMPLES = 10000;
    private static final int DROP_SAMPLES = 5000;
    private static final int DOWN_SAMPLE = 10;

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
     *
     * @return A list of samples of the posterior
     **/
    public static List<Integer[]> runKeanu(Integer[] truth, boolean createGraph) {

        System.out.println("Initialising random number stream");
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

        // Create the BayesNet and write it out?
        System.out.println("Creating BayesNet");
        BayesianNetwork net = new BayesianNetwork(box.getConnectedGraph());
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

        // Workaround for too many evaluations during sample startup
        //random.setAndCascade(random.getValue());

        // Sample from the posterior
        System.out.println("Sampling");
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(net, Arrays.asList(box), NUM_SAMPLES);

        // Get the number of people per iteration (an array of IntegerTensors) for each sample
        //List<Integer[]> samples = sampler.drop(DROP_SAMPLES).downSample(DOWN_SAMPLE).get(box).asList();
        List<Integer[]> samples = sampler.get(box).asList();

        return samples;
    }


    /* Main */

    public static void main (String[] args) {

        System.out.println("Starting. Number of iterations: " + NUM_ITER);

        // Make truth data
        System.out.println("Making truth data");
        VertexBackedRandomGenerator truthRandom = new VertexBackedRandomGenerator(NUM_RAND_DOUBLES, 0, 0);
        Integer[] truth = SimpleWrapper.runModel(truthRandom);

        System.out.println("Truth data length: " + truth.length);
        System.out.println(Arrays.asList(truth).toString() + "\n\n");

        //Run kenanu
        //ArrayList<Integer> obIntervals = new ArrayList<>(Arrays.asList(0,1));
        //obIntervals.parallelStream().forEach(i -> keanu(truth, i, timestamp, justCreateGraphs));
        List<Integer[]> samples = runKeanu(truth, true);

        System.out.println("Finished running. Ran " + samples.size() + " samples and " + models.size() + " models");




        // Write out random numbers used and the actual results
        Writer w1, w2;
        long time = System.currentTimeMillis();
        try {
            w1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Results_" + time + ".csv"), "utf-8"));
            w2 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "RandomNumbers_" + time + ".csv"), "utf-8"));

            // Do the truth data first
            for (int val : truth) w1.write(val + ","); // Values
            w1.write("\n");
            // (First model created is the truth model, hence get(0) )
            for (double rand : models.get(0).getRandomNumbers()) w2.write(rand + ",");
            w2.write("\n");

            // Now the samples. Results first, then the random numbers
            for (Integer[] sample : samples) {
                for (int val : sample) {
                    w1.write(val + ",");
                }
                w1.write("\n");
            }
            for (int i = 1; i < models.size(); i++) { // start from 1 as element 0 is the truth model
                for (double rand : models.get(i).getRandomNumbers()) {
                    w2.write(rand + ",");
                }
                w2.write("\n");
            }
            w1.close();
            w2.close();
        } catch (IOException ex) {
            System.out.println("Error writing to file");
        }

    }
}
