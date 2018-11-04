package SimpleModel.c;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.binary.BinaryOpLambda;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.PoissonVertex;

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

    //private static final int NUM_OBSERVATIONS = 5; // TEMPORARILY

    /* Model parameters */
    private static final UniformVertex threshold = new UniformVertex(-1.0, 1.0);
    //private static final PoissonVertex state = new PoissonVertex(1);
    private static final int NUM_ITER = 1001; // Total number of iterations

    /* Hyperparameters */
    private static final int WINDOW_SIZE = 200; // Number of iterations per update window
    private static final int NUM_WINDOWS = NUM_ITER / WINDOW_SIZE; // Number of update windows
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
            "\tWindow size: " + WINDOW_SIZE+"\n"+
            "\tNumber of windows: " +NUM_WINDOWS );

        // Initialise stuff
        Double truthThreshold = threshold.sample(KeanuRandom.getDefaultRandom()).getValue(0);

        /*
         ************ CREATE THE TRUTH DATA ************
         */

        System.out.println("Making truth data");

        // Generate truth data

        SimpleModel truthModel = new SimpleModel(truthThreshold , RAND_GENERATOR);
        Integer[] truthData = new Integer[NUM_ITER];
        int currentTruthState = 0; // initial state
        for (int i=0; i< NUM_ITER; i++) {
            truthData[i] = currentTruthState ;
            int newState = truthModel.step(currentTruthState );
            currentTruthState = newState;
        }

        System.out.println("SimpleModel configured with truth threshold: "+truthThreshold);
        System.out.println("Truth data length: " + truthData.length);
        System.out.println("Truth data: "+Arrays.asList(truthData).toString());
        System.out.println("Truth threshold is: "+truthThreshold);


        /*
         ************ START THE MAIN LOOP ************
         */
        int iter = 0; // Record the total number of iterations we have been through
        int currentStateEstimate = 0; // Save our estimate of the state at the end of the window. Initially 0
        //double currentThresholdEstimate = 0.0; // Save our threshold estimate


        for (int window = 0; window < NUM_WINDOWS; window++) { // Loop for every window

            System.out.println("Entering update window "+window);

            // Increment the counter of how many iterations the model has been run for
            iter += WINDOW_SIZE;

            /*
             ************ INITIALISE THE BLACK BOX MODEL ************
             */
            System.out.println("\tInitialising black box model");

            // XXXXNeed to get the current distribution of the state from the previous window ??
            // As doing parameter & state we need to calculate a joint distribition

            ConstantIntegerVertex state = new ConstantIntegerVertex(currentStateEstimate);
            BinaryOpLambda<DoubleTensor, IntegerTensor, Integer[]> box =
                new BinaryOpLambda<>(threshold, state, SimpleWrapperC::runModel);

            /*
             ************ OBSERVE SOME TRUTH DATA ************
             */
            // Get the relevant truth value (the one at the start of this update window)
            Integer truthValue = truthData[iter];
            System.out.println("\tObserving truth value "+truthValue+" (with noise "+SIGMA_NOISE+") for iteration "+iter);
            // output is the ith element of the model output (from box). Observe the most recent observation
            IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, WINDOW_SIZE-1);
            // output with a bit of noise. Lower sigma makes it more constrained.
            GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), SIGMA_NOISE);
            // Observe the output
            noisyOutput.observe(truthValue.doubleValue()); //.toDouble().scalar());


            /*
             ************ CREATE THE BAYES NET ************
             */

            // Create the BayesNet
            System.out.println("\tCreating BayesNet");
            BayesianNetwork net = new BayesianNetwork(box.getConnectedGraph());
            SimpleWrapperC.writeBaysNetToFile(net);


            // Workaround for too many evaluations during sample startup
            //random.setAndCascade(random.getValue());


            /*
             ************ SAMPLE FROM THE POSTERIOR************
             */

            // Sample from the posterior
            System.out.println("\tSampling");

            // Collect all the parameters that we want to sample
            List<Vertex> parameters = new ArrayList<>();
            parameters.add(box);
            parameters.add(threshold);
            //parameters.add(state);

            // Sample from the box and the random numbers
            NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(
                net,                // The bayes net with latent variables (the random numbers?)
                parameters,         // The vertices to include in the returned samples
                NUM_SAMPLES);       // The number of samples

            // Downsample etc
            sampler = sampler.drop(DROP_SAMPLES).downSample(DOWN_SAMPLE);


            /*
             ************ GET THE INFORMATION OUT OF THE SAMPLES ************
             */

            // ****** The threshold ******
            // Get the threshold estimates as a list (Convert from Tensors to Doubles)
            List<Double> thresholdSamples = sampler.get(threshold).asList().
                stream().map( (d) -> d.getValue(0)).collect(Collectors.toList());

            System.out.println("\tHave kept " + thresholdSamples.size()+" samples.");
            String theTime = String.valueOf(System.currentTimeMillis()); // So files have unique names
            // Write the threshold distribution
            SimpleWrapperC.writeThresholds(thresholdSamples, truthThreshold, theTime);

            // ****** The model states (out of the box) ******
            Integer[] truthWindow = Arrays.copyOfRange(truthData, iter-WINDOW_SIZE,iter); // The truth data for this window
            List<Integer[]> stateSamples = sampler.get(box).asList();
            assert truthWindow.length == stateSamples.get(0).length:
                String.format("Iteration lengths differ: truth:%s samples:%s", truthWindow.length, stateSamples.get(0).length);
             assert stateSamples.size() == thresholdSamples.size();
            SimpleWrapperC.writeResults(stateSamples, truthWindow, theTime);

            // Now estimate current state (mean of final states)
            XXXX HERE
            currentStateEstimate = stateSamples.stream((s) -> s).collect(Collectors.toList()).



        } // for update window




    } // main()



    /**
     * Run the SimpleModel for <code>WINDOW_SIZE</code> iterations and return the count at each iteration
     *
     **/
    public static Integer[] runModel(DoubleTensor threshold, IntegerTensor initialState) {

        SimpleModel s = new SimpleModel(threshold.getValue(0), RAND_GENERATOR);
        int state = initialState.getValue(0);
        Integer[] history= new Integer[WINDOW_SIZE];
        for (int i=0; i< WINDOW_SIZE; i++) {
            //history[i] = state;
            state = s.step(state); // new state
            history[i] = state;
        }
        assert history.length == WINDOW_SIZE;
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
