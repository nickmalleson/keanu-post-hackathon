package SimpleModel.c;

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
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SimpleWrapperB did parameter estimation and state estimation (the Bayes net estimated a posterior over all
 * model parameters) but only used the parameter estimates.
 *
 * This version of SimpleWrapper does state and parameter estimation *while the model is running* (i.e. rather than
 * doing so at the end), akin to data assimilation.
 */
public class SimpleWrapperC {

    private static final int NUM_OBSERVATIONS = 5; // TEMPORARILY

    /* Model parameters */
    private static final UniformVertex THRESHOLD = new UniformVertex(-1.0, 1.0);
    private static final int NUM_ITER = 1000;

    /* Hyperparameters */
    private static final int UPDATE_INTERVAL = 200; // Number of iterations between updates
    private static final double SIGMA_NOISE = 5.0; // Noise added to the observations
    private static final int NUM_SAMPLES = 2000; // Number of samples to MCMC
    private static final int DROP_SAMPLES = 1;
    private static final int DOWN_SAMPLE = 5;

    // Initialise the random number generator used throughout
    public static final int NUM_RAND_DOUBLES = 10000;
    private static final VertexBackedRandomGenerator RAND_GENERATOR =
        new VertexBackedRandomGenerator(NUM_RAND_DOUBLES,0,0);

    /* Admin parameters */
    private static String dirName = "results/simpleC/"; // Place to store results


    /**
     * Run the probabilistic model. This is the main function.
     **/
    public static void main (String[] args) {

        System.out.println("Starting.\n" +
            "\tNumber of iterations: " + NUM_ITER+"\n"+
            "\tUpdate interval: " + UPDATE_INTERVAL);

        // Initialise stuff
        Double truthThreshold = THRESHOLD.sample(KeanuRandom.getDefaultRandom()).getValue(0);
        SimpleModel.init(truthThreshold, RAND_GENERATOR);

        /*
         ************ CREATE THE TRUTH DATA ************
         */

        System.out.println("Making truth data");

        // Generate truth data

        // TODO replace with call to step(state,iter)
        int currentState = 0; // initial state
        Integer[] truthData = new Integer[NUM_ITER];
        for (int i=0; i< NUM_ITER; i++) {
            truthData[i] = currentState;
            int newState = SimpleModel.step(currentState);
            currentState = newState;
        }

        System.out.println("SimpleModel configured with truth threshold: "+SimpleModel.getThreshold());
        System.out.println("Truth data length: " + truthData.length);
        System.out.println("Truth data: "+Arrays.asList(truthData).toString());
        System.out.println("Truth threshold is: "+truthThreshold);

        /*
         ************ INITIALISE THE BLACK BOX MODEL ************
         */

        // This is the 'black box' vertex that runs the model.
        System.out.println("Initialising black box model");

        UnaryOpLambda<DoubleTensor, Integer[]> box =
            new UnaryOpLambda<>( THRESHOLD, SimpleWrapperC::runModel);

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
        SimpleWrapperC.writeBaysNetToFile(net);

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
        SimpleWrapperC.writeThresholds(thresholdSamples, truthThreshold, theTime);

        // Get the number of people per iteration for each sample
        List<Integer[]> peopleSamples = sampler.get(box).asList();
        assert peopleSamples.size() == thresholdSamples.size();
        System.out.println("Have saved " + peopleSamples.size()+" samples.");
        SimpleWrapperC.writeResults(peopleSamples , truthData, theTime);

    }



    /** Run the SimpleModel and return the count at each iteration **/
    public static Integer[] runModel(DoubleTensor threshold) {
        SimpleModel.setThreshold(threshold.getValue(0));
        int state = 0; // initial state
        Integer[] history= new Integer[NUM_ITER];
        for (int i=0; i< NUM_ITER; i++) {
            //history[i] = state;
            state = SimpleModel.step(state); // new state
            history[i] = state;
        }
        return history;
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


}
