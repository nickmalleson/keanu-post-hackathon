package SimpleModel.d;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.distributions.continuous.Gaussian;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.tensor.NumberTensor;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.binary.BinaryOpLambda;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.CastIntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.ProbabilisticInteger;
import org.apache.commons.math3.random.RandomGenerator;
import org.yaml.snakeyaml.scanner.Constant;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SimpleWrapperB did parameter estimation and state estimation (the Bayes net estimated a posterior over all
 * model parameters) but only used the parameter estimates.
 *
 * SimpleWrapperC did state and parameter estimation *while the model is running* (i.e. rather than
 * doing so at the end), akin to data assimilation.
 *
 * SimpleWrapper D has converted the SimpleModel to keanu native.
 *
 */
public class SimpleModelNative {

    //private static final int NUM_OBSERVATIONS = 5; // TEMPORARILY

    /* Model parameters */
    private static final UniformVertex threshold = new UniformVertex(-1.0, 1.0);
    //private static final PoissonVertex state = new PoissonVertex(1);
    private static final int NUM_ITER = 2000; // Total number of iterations

    /* Hyperparameters */
    private static final int WINDOW_SIZE = 20; // Number of iterations per update window
    private static final int NUM_WINDOWS = NUM_ITER / WINDOW_SIZE; // Number of update windows
    private static final double SIGMA_NOISE = 1.0; // Noise added to the observations
    private static final int NUM_SAMPLES = 2000; // Number of samples to MCMC
    private static final int DROP_SAMPLES = 1; // Burn-in period
    private static final int DOWN_SAMPLE = 5;  // Only keen every x sample

    // Initialise the random number generator used throughout
    public static final int NUM_RAND_DOUBLES = 10000;
    private static final VertexBackedRandomGenerator RAND_GENERATOR =
        new VertexBackedRandomGenerator(NUM_RAND_DOUBLES,0,0);

    /* Admin parameters */
    private static String dirName = "results/simpleD/"; // Place to store results

    /* Book keeping */
    private static boolean firstRun = true; // Horrible hack to do with writing files. Set to false after 1st window
    private static Writer thresholdWriter; // Writer for the threshold estimates
    private static Writer stateWriter; // Writer for the state estimates (i.e. 'results')

    /**
    public SimpleModelNative(double threshold, RandomGenerator random) {
        this.threshold = threshold;
        this.random = random;
    } */

    /**
     * Run the probabilistic model. This is the main function.
     **/
    public static void main(String[] args) throws Exception {

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
        System.out.println("Truth data: "+ Arrays.asList(truthData).toString());
        System.out.println("Truth threshold is: "+truthThreshold);

        initFiles(); // Get files ready to start writing results

        /*
         ************ START THE MAIN LOOP ************
         */
        int iter = 0; // Record the total number of iterations we have been through
        int currentStateEstimate = 0; // Save our estimate of the state at the end of the window. Initially 0
        double currentThresholdhEstimate = -1; //  Interesting to see what the threshold estimate is (not used in assimilation)
        double priorMu = 0;
        //double currentThresholdEstimate = 0.0; // Save our threshold estimate


        for (int window = 0; window < 1; window++) { // Loop for every window


            System.out.println(String.format("Entering update window: %s (iterations %s -> %s)", window, iter, iter+WINDOW_SIZE));
            System.out.println(String.format("\tCurrent state (at iter %s) estimate / actual: %s, %s: ", iter, currentStateEstimate, truthData[iter]));
            System.out.println(String.format("\tCurrent threshold estimate (for info): %.2f", currentThresholdhEstimate));

            // Increment the counter of how many iterations the model has been run for.
            // In the first window increment by WINDOW_SIZE-1 otherwise we run off the end of the truth array on the very last iteration
            iter += window==0 ? WINDOW_SIZE-1 : WINDOW_SIZE;

            /*
             ************ INITIALISE THE BLACK BOX MODEL ************
             */
            //System.out.println("\tInitialising black box model");

            // As doing parameter & state we need to calculate a joint distribition?

            // One way to keep information about the distribution is to represent the state as a
            // MultivariateGaussian. Can estimate that gaussian of the posterior (after observing
            // data) using the method that LUke and Dan discussed.

            /**
            ConstantIntegerVertex state = new ConstantIntegerVertex(currentStateEstimate);
            BinaryOpLambda<DoubleTensor, IntegerTensor, Integer[]> box =
                new BinaryOpLambda<>(threshold, state, SimpleWrapper::runModel);
            */

            // --------------------------------------------------------------------------
            /**
             * Logic here replaces runModel() and SimpleModel.step()
             */

            //SimpleModel s = new SimpleModel(threshold.getValue(0), RAND_GENERATOR);

            //ConstantIntegerVertex initialState = new ConstantIntegerVertex(currentStateEstimate);
            //IntegerVertex initialState = new ProbabilisticInteger(currentStateEstimate);
            //DoubleVertex initialState = new CastDoubleVertex(currentStateEstimate);
            DoubleVertex state = new GaussianVertex(priorMu, 1);


            //int state = initialState.getValue(0);
            //Integer[] history = new Integer[WINDOW_SIZE];

            List<DoubleVertex> history = new ArrayList<>();

            // Is the -1 still necessary here?
            //IntegerTensor hist = IntegerTensor.create(new int[WINDOW_SIZE-1]);


            /**
             * STEP LOGIC *
             */
            for (int i=0; i<WINDOW_SIZE; i++) {
                /**
                state = s.step(state);
                history[i] = state;
                */

                // Temporarily imagine that we know the threshold (later try to estimate this)
                state = RAND_GENERATOR.nextGaussian() > truthThreshold ? state.plus(1) : state.minus(1);

                history.add(state);


                //state = s.random.nextGaussian() > s.threshold ? state + 1 : state - 1;
                //state = RAND_GENERATOR.nextGaussian();

                //history[i] = state;
                //hist.setValue(state, i);
            }

/*
            System.out.println("Starting Loop...");
            for (int i=0; i<history.size(); i++) {
                DoubleVertex current = history.get(i);
                System.out.println(current);
                //System.out.println(current.getShape().toString());
                //System.out.println();
            }
            System.out.println("Loop Finished.");
            */

            System.out.println("Starting Loop 2: ForEach...");
            for (DoubleVertex current : history) {
                System.out.println(current);
                System.out.println(current.getValue(0));
                break;
            }
            System.out.println("Loop 2 Finished.");

            // Leaving these print checks and both copies of hist(ory) for clarity
            /*
            System.out.println(Arrays.toString(history));
            System.out.println("--------------------------------");
            System.out.println(Arrays.toString(hist.asFlatArray()));
            */

            state.setAndCascade(0); // Sets state value to 0 (known at start of model)

            // --------------------------------------------------------------------------


            /*
             ************ OBSERVE SOME TRUTH DATA ************
             */

            // Loop through and apply observations
            for (int i=0; i<WINDOW_SIZE; i++) {

                int t = window * (WINDOW_SIZE - 1) + i; // This is model iteration number

                DoubleVertex currentHist = history.get(i);

                GaussianVertex observedVertex = new GaussianVertex(currentHist, 1.0);
                observedVertex.observe(truthData[t]);
            }

            /*
            // Get the relevant truth value (the one at the end of this update window)
            Integer truthValue = truthData[iter];
            System.out.println("\tObserving truth value "+truthValue+" (with noise "+SIGMA_NOISE+") for iteration "+iter);

            //Integer output = history[WINDOW_SIZE-1];
            //IntegerTensor output = IntegerTensor.create(WINDOW_SIZE);
            //IntegerVertex output = new CastIntegerVertex(hist);

            // output with a bit of noise. Lower sigma makes it more constrained.
            //GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), SIGMA_NOISE);
            GaussianVertex noisyOutput = new GaussianVertex(, SIGMA_NOISE);
            // Observe the output
            noisyOutput.observe(truthValue.doubleValue()); //.toDouble().scalar());
            */

            /*
             ************ CREATE THE BAYES NET ************
             */


            // Create the BayesNet
            System.out.println("\tCreating BayesNet");
            BayesianNetwork net = new BayesianNetwork(state.getConnectedGraph());

            SimpleModelNative.writeBaysNetToFile(net);

            // Workaround for too many evaluations during sample startup
            //random.setAndCascade(random.getValue());


            /*
             ************ SAMPLE FROM THE POSTERIOR************
             */

            // Sample from the posterior
            System.out.println("\tSampling");

            // Collect all the parameters that we want to sample
            List<Vertex> parameters = new ArrayList<>();
            //parameters.add(box);
            parameters.add(threshold);
            parameters.add(state);

            // Sample from the box and the random numbers
            NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(
                net,                // The bayes net with latent variables
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

            // Find the mean threshold estimate (for info, not used)
            currentThresholdhEstimate =  thresholdSamples.stream().reduce(0d,(a,b) -> a+b) / thresholdSamples.size();
            // System.out.println("\tHave kept " + thresholdSamples.size()+" samples.");

            // Write the threshold distribution
            SimpleModelNative.writeThresholds(thresholdSamples, truthThreshold, window, iter);

            // ****** The model states (out of the box) ******
            //Integer[] truthWindow = Arrays.copyOfRange(truthData, iter-WINDOW_SIZE,iter); // The truth data for this window
            //List<Integer[]> stateSamples = sampler.get(state).asList();
            List<DoubleTensor> stateSamples = sampler.get(state).asList();

            /*
            System.out.println(sampler.get(state).asList().toArray()[1].toString());

            System.out.println("List<DoubleTensor>: " + stateSamples.size());
            System.out.println("-------------------------------------");
            System.out.println(stateSamples.toString());
            System.out.println("-------------------------------------");
            */

            /*
            for (int i=0; i<stateSamples.size(); i++) {
                System.out.println("---------------------");
                DoubleTensor current = stateSamples.get(i);
                System.out.println(current);
                System.out.println(current.getValue(i));
                System.out.println("---------------------");
            }
            */


            //List<DoubleVertex> stateSamples = sampler.get(state).asList();

            /*
            System.out.println("PRINT CHECKS...");
            System.out.println(stateSamples.size());
            System.out.println("---------------------");
            System.out.println(stateSamples.get(0));
            System.out.println("---------------------");
            */

            //assert truthWindow.length == stateSamples.get(0).length:
            //    String.format("Iteration lengths differ: truth:%s samples:%s", truthWindow.length, stateSamples.get(0).length);
            assert stateSamples.size() == thresholdSamples.size();
            //SimpleWrapperC.writeResults(stateSamples, truthWindow);
            //SimpleModelNative.writeResults(stateSamples, truthData);

            // Now estimate current state (mean of final states).

            // Want the maximum probability. Could do:
            // 1 - max value of the KDE



            //currentStateEstimate = stateSamples.stream((s) -> s).collect(Collectors.toList()).
            // Get the last value from each sample (i.e. the final iteration) and calculate the sum of these
            /*
            int stateSum = stateSamples.stream().map(l -> l[l.getLength()-1]).reduce(0,(a,b) -> a+b);
            double mean = (double) stateSum / stateSamples.size();
            currentStateEstimate = (int) Math.round(mean);
            */

            DoubleTensor posterior = state.getValue();
            priorMu = posterior.scalar();



            firstRun = false; // To say the first window has finished (horrible hack to do with writing files)


        } // for update window


        SimpleModelNative.closeResultsFiles();
    }




    /*
     **************** ADMIN STUFF ****************
     */

    private static void writeThresholds(List<Double> randomNumberSamples, Double truthThreshold, int window, int iteration) {

        // Write out random numbers used and the actual results
        try {
            if (firstRun) { // If true then the files have just been initialised. Write the truth data to be the first row.
                thresholdWriter.write("-1,-1,"+ truthThreshold + ",\n"); // Truth value (one off)
            }

            // Now the samples.
            for (double sample : randomNumberSamples) {
                thresholdWriter.write(String.format("%s, %s, %s, ", window, iteration, sample));
                thresholdWriter.write("\n");
            }
        } catch (IOException ex) {
            System.err.println("Error writing thresholds to file: "+ ex.getMessage());
            ex.printStackTrace();
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
            System.err.println("Error writing graph to file: "+ ex.getMessage());
            ex.printStackTrace();
        }
    }

    /* Writing these results is a real hack because now that this is called at every window the results
    need to be cached and all written at once. Each iteration is stored as a new column which is a pain.*/
    //private static List<Integer[]> samplesHistory;
    private static List<DoubleTensor> samplesHistory;
    private static void writeResults(List<DoubleTensor> samples, Integer[] truth) {

        // Write out the model results (people per iteration)
        try {
            if (firstRun) { // Files have just been initialised so do the truth data first
                for (int val : truth) {
                    stateWriter.write(val + ","); // Values
                }
                stateWriter.write("\n");

                // (Hack because the samples now need to be cached after each window)
                // Initialise from the samples from the first window
                samplesHistory = samples;
                return;
            }
            // Cache the samples. They are written when closeFiles() is called. The results from each
            // sample (a list of integers representing the state) need to be appended to the end of their arrays.
            assert samples.size() == samplesHistory.size(); // The number of samples hasn't changed
            //for (int sampleNumber = 0; sampleNumber< samples.size(); sampleNumber++) {
            for (int sampleNumber = 0; sampleNumber< 10; sampleNumber++) {
                //Integer[] originalSample = samplesHistory.get(sampleNumber);
                //Integer[] newStatesToBeAdded = samples.get(sampleNumber);
                DoubleTensor originalSample = samplesHistory.get(sampleNumber);
                DoubleTensor newStatesToBeAdded = samples.get(sampleNumber);

                /*
                System.out.println("----------------------------");
                System.out.println(originalSample.asFlatList());
                //System.out.println(newStatesToBeAdded);
                System.out.println("----------------------------");
                //System.out.println(newStatesToBeAdded.getLength());
                //System.out.println("----------------------------");
                System.out.println(originalSample.getShape());
                System.out.println("----------------------------");
                */

                // Add these new states (the state history) to the existing sample
                // This is painful because the sample history is an array not an ArrayList
                // Make a new array that is large enough to hold the new history for this sample

                //Integer[] newSample = Arrays.copyOf(originalSample, originalSample.length+WINDOW_SIZE);
                
                DoubleTensor newSample = (DoubleTensor) originalSample.duplicate();
                // Copy the new states to the end of the sample
                assert newSample.getLength() == originalSample.getLength() + WINDOW_SIZE :
                    String.format("New: %s, Original: %s (window size: %s)",newSample.getLength(), originalSample.getLength(), WINDOW_SIZE);
                //System.out.println(String.format("New: %s, Original: %s (window size: %s)",newSample.getLength(), originalSample.getLength(), WINDOW_SIZE));
                for (int i = 0; i< WINDOW_SIZE; i++) {
                    //newSample[i+originalSample.length] = newStatesToBeAdded[i];
                    int len = (int) originalSample.getLength();

                    double newValue = newStatesToBeAdded.getValue(i);

                    newSample.setValue(newValue, i+len);

                    //newSample.setValue(newStatesToBeAdded.getValue(i), i+len);
                }
                // Replace the old sample with the new one
                samplesHistory.set(sampleNumber, newSample);
            }
        } catch (IOException ex) {
            System.err.println("Error writing states to file: "+ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void initFiles() throws IOException {
        String theTime = String.valueOf(System.currentTimeMillis()); // So files have unique names

        thresholdWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(dirName + "Params_" + theTime + ".csv"), "utf-8"));

        thresholdWriter.write("Window, Iteration, Threshold,\n");

        stateWriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(dirName + "Results_" + theTime + ".csv"), "utf-8"));

    }

    private static void closeResultsFiles() throws Exception {
        System.out.println("Closing output files.");

        // Write the cached samples
        for (DoubleTensor sample : samplesHistory) {
            for (int i=0; i<sample.getLength(); i++) {
                stateWriter.write(sample.getValue(i) + ",");
            }
            stateWriter.write("\n");
        }

        thresholdWriter.close();
        stateWriter.close();
    }
}
