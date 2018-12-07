package SimpleModel.d;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.VertexSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.GradientOptimizer;
import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertexSamples;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NativeModel {

    /* Model Parameters */
    private static final UniformVertex threshold = new UniformVertex(-1.0, 1.0);
    private static final int NUM_ITER = 2000; // Total No. of iterations

    /* Hyperparameters */
    private static final int WINDOW_SIZE = 200; // Number of iterations per update window
    private static final int NUM_WINDOWS = NUM_ITER / WINDOW_SIZE; // Number of update windows
    private static final double SIGMA_NOISE = 1.0; // Noise added to observations
    private static final int NUM_SAMPLES = 2000; // Number of samples to MCMC
    private static final int DROP_SAMPLES = 1; // Burn in period
    private static final int DOWN_SAMPLE = 5; // Only keep every x sample

    // Initialise random number generator used throughout
    public static final int NUM_RANDOM_DOUBLES = 10000;
    private static final VertexBackedRandomGenerator RAND_GENERATOR =
        new VertexBackedRandomGenerator(NUM_RANDOM_DOUBLES, 0, 0);

    /* Admin parameters */
    private static String dirName = "results/simpleD/"; // Place to store results

    /* Bookkeeping parameters */
    private static boolean firstRun = true; // Horrible hack to do with writing files. Set to false after first window
    private static Writer thresholdWriter; // Writer for the threshold estimates
    private static Writer stateWriter; // Writer for the state estimates (i.e. results)


    /**
     * - Why do we get 0 for the currentStateEstimate? (e.g. it prints
     *      "Current state (at iter 379) estimate / actual: 0, -135.82235695345645:  “
     *
     * A - currentStateEstimate was not reassigned at end of loop, forgot to transfer that code to NativeModel
     *      FIX -  getValue() from state variable at end of main loop and assign to currentStateEstimate
     *
     *
     * - Do the graphs that it outputs make sense? (I know they’re difficult to understand,
     *      but do the number of nodes correspond with the number of nodes we’d expect to have
     *      once we’ve built up the Bayes net, and if you do something like change the number
     *      of observations does the graph change as you’d expect?).
     *
     * A - Graphs seem to make sense. WINDOW_SIZE dictates how many GaussianVertices are included (as well as
     *      AdditionV, DifferenceV, and ConstantDoubleVert).
     *
     *
     * - Is the state estimation actually working? You can’t write out the results file yet,
     *      but you could do something hacky like take the mean of the sample estimates of
     *      the state (e..g. the mean of the ‘stateSamplesDouble’ list we made) and see how
     *      this changes in each window, compared to the truth value and to the ‘posterior’
     *      that we calculate (right at the end of the window loop).
     *
     * A - stateSamplesMean is now printed out at the end of every update window. Seems to be reliably within
     *      5-10% of truthValue at end (often more accurate than this).
     *
     *
     * - In the Lorenz model they don’t sample because the model has been specified entirely
     *      probabilistically. Could you try to get the posterior without sampling, and compare
     *      this to the state estimate that we get with sampling?
     *
     * A - Need to talk about this point as I'm not sure I understand fully. If our posterior is taken directly
     *      from state.getValue(), and our samples are stored in stateSamplesDouble, how do the samples affect
     *      the posterior when the two do not interact? Are we not already getting the posterior without sampling,
     *      and the sampling is just used for us to calculate a state estimate?
     */



    /** Run the probabilistic model. This is the main function.
     *
     **/
    public static void main(String[] args) throws Exception {

        System.out.println("Starting.\n" +
            "\tNumber of iterations: " + NUM_ITER +"\n"+
            "\tWindow size: " + WINDOW_SIZE +"\n"+
            "\tNumber of windows: " + NUM_WINDOWS);

        // Initialise stuff
        Double truthThreshold = threshold.sample(KeanuRandom.getDefaultRandom()).getValue(0);

        /*
         ************ CREATE THE TRUTH DATA ************
         */

        System.out.println("Making truth data");

        // Generate truth data

        List<DoubleVertex> truthData = new ArrayList<>();
        // TODO: Replace this sigma with SIGMA_NOISE?
        DoubleVertex currentTruthState = new GaussianVertex(0, 1.0);
        for (int i=0; i < NUM_ITER; i++) {
            currentTruthState =
                RAND_GENERATOR.nextGaussian() > truthThreshold ? currentTruthState.plus(1) : currentTruthState.minus(1);
            // add state to history
            truthData.add(currentTruthState);
        }

        System.out.println("SimpleModel configured with truth threshold: "+ truthThreshold);
        System.out.println("Truth data length: " + truthData.size());
        System.out.println("Truth data: ");
        for (DoubleVertex aTruthData : truthData) {
            System.out.print(aTruthData.getValue(0) + ",  ");
        }
        System.out.println("\nTruth threshold is: "+ truthThreshold);

        initFiles(); // Get files ready to start writing results

        /*
         ************ START THE MAIN LOOP ************
         */
        int iter = 0; // Record the total number of iterations we have been through
        double currentStateEstimate = 0.0; // Save our estimate of the state at the end of the window. Initially 0
        double currentThresholdEstimate = -1.0; //  Interesting to see what the threshold estimate is (not used in assimilation)
        double priorMu = 0.0;


        // TODO: Replace this loop with a while loop (Need to calculate error w/ each iteration and stop loop when error is low enough)
        for (int window=0; window < NUM_WINDOWS; window++) {

            System.out.println(String.format("Entering update window: %s (iterations %s -> %s)", window, iter, iter+WINDOW_SIZE));
            System.out.println(String.format("\tCurrent state (at iter %s) estimate / actual: %s, %s: ",
                iter,
                currentStateEstimate,
                truthData.get(iter).getValue(0)));
            System.out.println(String.format("\tCurrent threshold estimate (for info): %.2f", currentThresholdEstimate));

            //Increment the counter with how many iterations the model has been run for.
            // In first window increment by -1 otherwise we run off end of truth array on last iteration
            iter += window==0 ? WINDOW_SIZE-1 : WINDOW_SIZE;

            /*
             ************ INITIALISE STATE ************
             */

            // TODO: Look at how this DoubleVertex is stored further on (possibly change)
            /*
            state has now changed type from int to DoubleVertex
            This changes how state is stored later on in Tensors
             */
            DoubleVertex state = new GaussianVertex(priorMu, 1.0);

            /*
            history changed type from Integer[] to List<DoubleVertex> (due to state change)
            How does this affect downstream history?
             */
            List<DoubleVertex> history = new ArrayList<>();

            /*
             ************ STEP ************
             */

            for (int i=0; i < WINDOW_SIZE; i++) {

                // Temporarily imagine that we know the threshold (later try to estimate this)
                // TODO: Try to estimate threshold here and not pass explicitly
                state = RAND_GENERATOR.nextGaussian() > truthThreshold ? state.plus(1) : state.minus(1);
                // add state to history
                history.add(state);
            }

            state.setAndCascade(0); // Sets state value to 0 (known at start of model)

            /*
             ************ OBSERVE SOME TRUTH DATA ************
             */

            // Loop through and apply some observations
            for (int i=0; i < WINDOW_SIZE; i++) {

                // This is model iteration number:
                int t = window==0 ?
                    window * (WINDOW_SIZE - 1) + i : // on first iteration reduce the window size by 1
                    window * (WINDOW_SIZE ) + i;

                //if (firstRun) { t -= 1; }
                //t==0 ? WINDOW_SIZE-1 : WINDOW_SIZE;

                DoubleVertex currentHist = history.get(i);

                GaussianVertex observedVertex = new GaussianVertex(currentHist, SIGMA_NOISE);
                //System.out.println(String.format("%s %s", i, t));
                //System.out.println(truthData.size());
                observedVertex.observe(
                    truthData.get(t).
                    getValue(0));
            }

            /*
             ************ CREATE THE BAYES NET ************
             */

            // Create the BayesNet
            System.out.println("\tCreating BayesNet");
            BayesianNetwork net = new BayesianNetwork(state.getConnectedGraph());

            // write Bayes net
            writeBayesNetToFile(net);

            /*
             ************ OPTIMISE ************
             */

            System.out.println("\t\tOptimising with Max A Posteriori");
            System.out.println("\t\tPrevious state value: " + state.getValue(0));

            System.out.println("\t\tRunning Max A Posteriori...");

            GradientOptimizer netOptimiser = new GradientOptimizer(net);
            netOptimiser.maxAPosteriori();

            System.out.println("\t\tNew state value: " + state.getValue(0));

            /*
             ************ SAMPLE FROM THE POSTERIOR ************
             */

            // Sample from the posterior
            System.out.println("\tSampling");

            // Collect all the parameters that we want to sample
            List<Vertex> parameters = new ArrayList<>();
            parameters.add(threshold);

            parameters.add(state);

            // Sample from the net and the random numbers
            NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(
                net,                // The bayes net with latent variables
                parameters,         // The vertices to include in the returned samples
                NUM_SAMPLES);       // The number of samples

            /*
            NetworkSamples sampler2 = MetropolisHastings.getPosteriorSamples(
                net,
                net.getLatentVertices(),
                NUM_SAMPLES);
            */

            // Downsample etc
            sampler = sampler.drop(DROP_SAMPLES).downSample(DOWN_SAMPLE);


            /*
             ************ GET THE INFORMATION OUT OF THE SAMPLES ************
             */

            // TODO: PROBLEM AREA. Possibly convert all List<DoubleTensor> to List<Double>?

            // ****** The threshold ******
            // Get the threshold estimates as a list (Convert from Tensors to Doubles)
            List<Double> thresholdSamples = sampler.get(threshold).asList().
                stream().map( (d) -> d.getValue(0)).collect(Collectors.toList());

            // Find the mean threshold estimate (for info, not used)
            currentThresholdEstimate = thresholdSamples.stream().reduce(0d, (a,b) -> a+b) / thresholdSamples.size();

            // Write the threshold distribution
            NativeModel.writeThresholds(thresholdSamples, truthThreshold, window, iter);

            // TODO: Fix this so that the contents of the List and DoubleTensors can be accessed easily later on
            // ****** The model states (out of the box) ******
            List<DoubleTensor> stateSamples = sampler.get(state).asList();
            //System.out.println(stateSamples);


            // Previous attempts:
            //List<DoubleVertex> stateSamples = sampler.get(history).asList();
            //List<DoubleVertex> stateSamples = sampler.get(history)
            //System.out.println(stateSamplesDouble.toString());
            //List<DoubleTensor> stateSamples = sampler.getDoubleTensorSamples(state).asList();

            List<Double> stateSamplesDouble = sampler.get(state).asList().
                stream().map( (d) -> d.getValue(0)).collect(Collectors.toList());


            // Get mean of stateSamples to compare to truthValue and posterior
            double stateSum = stateSamplesDouble.stream().mapToDouble(Double::doubleValue).sum();
            double stateSamplesMean = stateSum / stateSamplesDouble.size();


            assert stateSamples.size() == thresholdSamples.size();

            //NativeModel.writeResults(stateSamples, truthData);

            // Get posterior distribution, extract and assign Mu
            DoubleTensor posterior = state.getValue();
            priorMu = posterior.scalar();



            // Get last value from each iteration and assign to state estimate
            //double finalState = state.getValue(0);
            //currentStateEstimate = (int) Math.round(finalState);

            currentStateEstimate = state.getValue(0);


            // Print out interesting values
            System.out.println("\tstateSamplesMean: " + stateSamplesMean);
            //System.out.println("\tPosterior: " + posterior.getValue(0));
            System.out.println("\tposterior == priorMu == currentStateEstimate == " + priorMu);
            System.out.println("\tCurrent truth state: " + truthData.get(iter).getValue(0));
            //System.out.println("\tcurrentStateEstimate: " + currentStateEstimate);


            firstRun = false; // To say the first window has finished (horrible hack to do with writing files)

        } // For update window

        //NativeModel.closeResultsFiles();
    }

    /*
     **************** ADMIN STUFF ****************
     */

    private static void writeThresholds(List<Double> randomNumberSamples, Double truthThreshold, int window, int iteration) {

        // Write out random numbers used and the actual results
        try {
            if (firstRun) { // If true then the files have just been initialised. Write the truth data to be the first row
                thresholdWriter.write("-1,-1," + truthThreshold + ",\n"); // Truth value (one off)
            }

            // Now the samples
            for (double sample : randomNumberSamples) {
                thresholdWriter.write(String.format("%s, %s, %s, ", window, iteration, sample));
                thresholdWriter.write("\n");
            }
        } catch (IOException ex) {
            System.err.println("Error writing thresholds to file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private static void writeBayesNetToFile(BayesianNetwork net) {
        try {
            Writer graphWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Graph_" + System.currentTimeMillis() + ".dot"), "utf-8"));
            graphWriter.write(GraphvizKt.toGraphvizString(net, new HashMap<>()));
            graphWriter.close();
        } catch (IOException ex) {
            System.err.println("Error writing graph to file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private static List<Double> samplesHistory;
    private static void writeResults(List<Double> samples, List<DoubleVertex> truth) {

        // Write out the model results (people per iteration)
        try {
            if (firstRun) { // Files have just been initialised so do the truth data first
                for (DoubleVertex val : truth) {
                    stateWriter.write(val.getValue(0) + ","); // Values
                }
                stateWriter.write("\n");

                // Hack because the samples now need to be cached after each window
                // Initialise from the samples from the first window
                samplesHistory = samples;
                return;
            }
            // Cache the samples. They are written when closeFiles() is called. The results from each
            // sample (a list of integers representing the state) need to be appended to the end of their arrays.

            assert samples.size() == samplesHistory.size(); // The number of samples hasn't changed

            for (int sampleNumber = 0; sampleNumber < samples.size(); sampleNumber++) {

                Double originalSample = samplesHistory.get(sampleNumber);
                Double newStatesToBeAdded = samples.get(sampleNumber);

                // TODO: Problem line below. newSample has to == originalSample plus WINDOW_SIZE
                //DoubleTensor newSample = (DoubleTensor) originalSample.duplicate();
                // OLD CODE:
                // Integer[] newSample = Arrays.copyOf(originalSample, originalSample.length + WINDOW_SIZE);

                // Copy the new states to the end of the sample
                //assert newSample.getLength() == originalSample.getLength() + WINDOW_SIZE :
                    //String.format("New: %s, Original: %s (window size: %s)",newSample.getLength(), originalSample.getLength(), WINDOW_SIZE);

                for (int i=0; i < WINDOW_SIZE; i++) {

                    // TODO: Fix this block; original logic commented below

                    //original: newSample[i+originalSample.length] = newStatesToBeAdded[i];

                }
                // Replace the old sample with the new one
                //samplesHistory.set(sampleNumber, newSample);
            }
        } catch (IOException ex) {
            System.err.println("Error writing states to file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private static void initFiles() throws IOException {
        String theTime = String.valueOf(System.currentTimeMillis()); // So files have unique names

        thresholdWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(dirName + "Params_" + theTime + ".csv"), "utf-8"));

        thresholdWriter.write("Window, Iteration, Threshold,\n");

        thresholdWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(dirName + "Params_" + theTime + ".csv"), "utf-8"));
    }


    /*
    private static void closeResultsFiles() throws Exception {
        System.out.println("Closing output files.");

        // Write the cached samples
        for (Double sample : samplesHistory) {
            for (int i=0; i< sample.size(); i++) {
                stateWriter.write(sample.getValue(i) + ",");
            }
            stateWriter.write("\n");
        }

        thresholdWriter.close();
        stateWriter.close();
    }
    */
}
