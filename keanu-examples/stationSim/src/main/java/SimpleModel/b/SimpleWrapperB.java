package SimpleModel.b;

import SimpleModel.a.SimpleModel;
import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Version of the simple wrapper that tries to calibrate on the
 * threshold parameter rather than the random numbers.
 */
public class SimpleWrapperB {

    /* Model parameters */
    private static final UniformVertex THRESHOLD = new UniformVertex(-1.0, 1.0);


    public static final int NUM_RAND_DOUBLES = 100000;
    private static final int NUM_ITER = 1000;

    // Initialise the random number generator used throughout
   private static final VertexBackedRandomGenerator rand =
        new VertexBackedRandomGenerator(NUM_RAND_DOUBLES,0,0);

    /* Hyperparameters */
    private static final double SIGMA_NOISE = 10;
    private static final int NUM_SAMPLES = 1000;
    private static final int DROP_SAMPLES = 1;
    //private static final int DROP_SAMPLES = NUM_SAMPLES/4;
    private static final int DOWN_SAMPLE = 5;

    private static final int NUM_OBSERVATIONS = 5; // Number of points to observe (temporary - will be replaced with proper tests)

    private static ArrayList<SimpleModel> models = new ArrayList<>(); // Keep all the models for analysis later

    /* Admin parameters */
    private static String dirName = "results/simpleB/"; // Place to store results


    /** Run the SimpleModel and return the count at each iteration **/
    public static Integer[] runModel(DoubleTensor threshold) {
        SimpleModel s = new SimpleModel(threshold.getValue(0), SimpleWrapperB.rand);

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

        // Generate truth data
        Double truthThreshold = THRESHOLD.sample(KeanuRandom.getDefaultRandom()).getValue(0);
        SimpleModel s = new SimpleModel(truthThreshold, SimpleWrapperB.rand);

        for (int i=0; i<NUM_ITER; i++) {
            s.step();
        }
        SimpleWrapperB.models.add(s);
        Integer[] truthData = s.getHistory();

        System.out.println("Truth data length: " + truthData.length);
        System.out.println("Truth data: "+Arrays.asList(truthData).toString() + "\n\n");
        System.out.println("Truth threshold is: "+truthThreshold);



        /*
         ************ INITIALISE THE BLACK BOX MODEL ************
         */

        // This is the 'black box' vertex that runs the model.
        System.out.println("Initialising black box model");

        UnaryOpLambda<DoubleTensor, Integer[]> box =
            new UnaryOpLambda<>( THRESHOLD, SimpleWrapperB::runModel);

        /*
         ************ OBSERVE SOME TRUTH DATA ************
         */

        // Observe the truth data plus some noise?
        System.out.println("Observing truth data. Adding noise with standard dev: " + SIGMA_NOISE);
        System.out.println("Observing at iterations: ");
        for (Integer i = 0; i < NUM_ITER; i+=NUM_ITER/ NUM_OBSERVATIONS) {
            System.out.print(i+",");
            // output is the ith element of the model output (from box)
            IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
            // output with a bit of noise. Lower sigma makes it more constrained.
            GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), SIGMA_NOISE);
            // Observe the output
            noisyOutput.observe(truthData[i].doubleValue()); //.toDouble().scalar());
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
        // Add the threshold parameter to the list
        List<DoubleTensor> samples = sampler.get(THRESHOLD).asList();
        // Get the threshold estimates as a list (Convert from Tensors to Doubles)
        List<Double> thresholdSamples =
            samples.stream().map( (d) -> d.getValue(0)).collect(Collectors.toList());

        String theTime = String.valueOf(System.currentTimeMillis()); // So files have unique names
        SimpleWrapperB.writeThresholds(thresholdSamples, truthThreshold, theTime);

        // Get the number of people per iteration for each sample
        List<Integer[]> peopleSamples = sampler.get(box).asList();
        System.out.println("Have saved " + peopleSamples.size() + " samples and ran " + models.size() + " models");
        SimpleWrapperB.writeResults(peopleSamples , truthData, theTime);

    }





    /*
     **************** ADMIN STUFF ****************
     */

    private static void writeThresholds(List<Double> randomNumberSamples, Double truthThreshold, String name) {

        // Write out random numbers used and the actual results
        Writer w1;
        try {
            w1 = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Params_" + name + ".csv"), "utf-8"));

            // Do the truth data first
            w1.write(truthThreshold + ",");
            w1.write("\n");

            // Now the samples.
            for (double sample : randomNumberSamples) {
                w1.write(sample+", ");
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
