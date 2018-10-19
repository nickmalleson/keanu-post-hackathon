package SimpleModel;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.ConstantGenericVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Version of the simple wrapper that tries to calibrate on the
 * threshold parameter rather than the random numbers.
 */
public class SimpleWrapperB {

    /* Model parameters */
    private static final ConstantGenericVertex THRESHOLD = new ConstantGenericVertex<Double> (0.2);
    public static final int NUM_RAND_DOUBLES = 100000;
    private static final int NUM_ITER = 1000;

    // Initialise the random number generator used throughout
   private static final VertexBackedRandomGenerator rand =
        new VertexBackedRandomGenerator(NUM_RAND_DOUBLES,0,0);

    /* Hyperparameters */
    private static final double SIGMA_NOISE = 0.1;
    private static final int NUM_SAMPLES = 1000;
    private static final int DROP_SAMPLES = 1;
    //private static final int DROP_SAMPLES = NUM_SAMPLES/4;
    private static final int DOWN_SAMPLE = 5;

    private static final int numObservations = 5; // Number of points to observe (temporary - will be replaced with proper tests)

    private static ArrayList<SimpleModel> models = new ArrayList<>(); // Keep all the models for analysis later

    //private static final long SEED = 1l;
    //private static final RandomGenerator random = new MersenneTwister(SEED);



    /* Admin parameters */
    private static String dirName = "results/simpleB/"; // Place to store results


    /** Run the SimpleModel and return the count at each iteration **/

    public static Integer[] runModel(ConstantGenericVertex threshold) {
        SimpleModel s = new SimpleModel((Double)threshold.getValue(), SimpleWrapperB.rand);

        for (int i=0; i<NUM_ITER; i++) {
            s.step();
        }
        SimpleWrapperB.models.add(s);
        return s.getHistory();
    }


    /**
     * Run the probabilistic model
     **/
    public static void run() {
        System.out.println("Starting. Number of iterations: " + NUM_ITER);

        /*
         ************ CREATE THE TRUTH DATA ************
         */

        System.out.println("Making truth data");

        // Run the model
        Integer[] truth = SimpleWrapperB.runModel(THRESHOLD);

        System.out.println("Truth data length: " + truth.length);
        System.out.println("Truth data: "+Arrays.asList(truth).toString() + "\n\n");


        /*
         ************ INITIALISE THE BLACK BOX MODEL ************
         */


        // This is the 'black box' vertex that runs the model.
        System.out.println("Initialising black box model");

        UnaryOpLambda<ConstantGenericVertex, Integer[]> box =
            new UnaryOpLambda<ConstantGenericVertex, Integer[]> ( THRESHOLD, SimpleWrapperB::runModel);



        /*
         ************ OBSERVE SOME TRUTH DATA ************
         */


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


        /*
         ************ CREATE THE BAYES NET ************
         */

        // Create the BayesNet
        System.out.println("Creating BayesNet");
        BayesianNetwork net = new BayesianNetwork(box.getConnectedGraph());
        SimpleWrapperB.writeBaysNetToFile(net);

        // Workaround for too many evaluations during sample startup
        //random.setAndCascade(random.getValue());


        /*
         ************ SAMPLE FROM THE POSTERIOR************
         */

        // Sample from the posterior
        System.out.println("Sampling");

        // Collect all the parameters that we want to sample (the random numbers and the box model)
        List<Vertex> parameters = new ArrayList<>(NUM_RAND_DOUBLES+1); // Big enough to hold the random numbers and the box
        parameters.add(box);
        parameters.add(THRESHOLD);

        // Sample from the box and the random numbers
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(
            net,                // The bayes net with latent variables (the random numbers?)
            parameters,         // The vertices to include in the returned samples
            NUM_SAMPLES);       // The number of samples

        // Sample using a stream.
        /*
        NetworkSamples sampler = MetropolisHastings.generatePosteriorSamples(
            net,                // The bayes net with latent variables (the random numbers?)
            parameters          // The vertices to include in the returned samples
        )
            .dropCount(DROP_SAMPLES)
            .downSampleInterval(DOWN_SAMPLE)
            .stream()
            .limit(NUM_SAMPLES)
            .map(networkState -> {
                for()
                    networkState.get(x)
            })
            .average().getAsDouble();
            */

        System.out.println("Finished running MCMC.");

        // Downsample etc
        sampler = sampler.drop(DROP_SAMPLES).downSample(DOWN_SAMPLE);


        /*
         ************ GET THE INFORMATION OUT OF THE SAMPLES ************
         */

        // Get the threshold estimates. A 2D list. First dimension holds the threshold (one item), second dimensions holds its samples.
        //List<List<Double>> randomNumberSamples = new ArrayList<List<Double>>(NUM_SAMPLES);
        List<List<Double>> thresholdSamples = new ArrayList<>(NUM_SAMPLES);
        // Add the threshold parameter to the list
        for (int i=0; i<1; i++) { // (1 because only 1 threshold parameter)
            //List<DoubleTensor> randSamples = sampler.get(randNumbers.get(i)).asList();
            List<ConstantGenericVertex> samples = sampler.get(THRESHOLD).asList();
            // Convert from Vertices to Doubles
            List<Double> samplesDouble =
                samples.stream().map( (d) -> (Double)d.getValue()).collect(Collectors.toList());
            thresholdSamples.add(samplesDouble);
        }

        String theTime = String.valueOf(System.currentTimeMillis()); // So files have unique names
        SimpleWrapperB.writeThresholds(thresholdSamples, (Double)THRESHOLD.getValue(), theTime);

        // Get the number of people per iteration for each sample
        List<Integer[]> peopleSamples = sampler.get(box).asList();
        System.out.println("Have saved " + peopleSamples.size() + " samples and ran " + models.size() + " models");
        SimpleWrapperB.writeResults(peopleSamples , truth, theTime);

    }





    /*
     **************** ADMIN STUFF ****************
     */

    private static void writeThresholds(List<List<Double>> randomNumberSamples, Double truthThreshold, String name) {

        // Write out random numbers used and the actual results
        Writer w1;
        try {
            w1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Rands_" + name + ".csv"), "utf-8"));

            // Do the truth data first
            w1.write(truthThreshold + ",");
            w1.write("\n");

            // Now the samples.
            for (int sample = 0; sample<randomNumberSamples.get(0).size(); sample++) {
                for (int d = 0; d<NUM_RAND_DOUBLES; d++) {
                    w1.write(randomNumberSamples.get(d).get(sample)+", ");
                }
                w1.write("\n");
            }
            w1.close();
        } catch (IOException ex) {
            System.out.println("Error writing to file");
        }
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

    private static void writeResults(List<Integer[]> samples, Integer[] truth, String name) {

        // Write out the model results (people per iteration)
        Writer w1;
        try {
            w1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Results_" + name + ".csv"), "utf-8"));

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

        SimpleWrapperB.run();
    }
}
