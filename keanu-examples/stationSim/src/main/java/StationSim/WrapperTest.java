package StationSim;



import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nick on 22/06/2018.
 */
public class WrapperTest {

    static Station stationSim = new Station(System.currentTimeMillis());
    private static int numTimeSteps = 1000;
    public static int numRandomDoubles = 10;
    private static int numSamples = 7000;
    private static int dropSamples = 1000;
    private static int downSample = 3;
    //private static boolean OBSERVE = true;
    private static double sigmaNoise = 0.1 ; // The amount of noise to be added to the truth

    public static void writeResults(List<Integer[]> samples, Integer[] truth, Boolean observed, int obInterval) {
        Writer writer = null;
        Station tempStation = new Station(System.currentTimeMillis());
        int totalNumPeople = tempStation.getNumPeople();
        int numOutputs = 6;


        String dirName = "results/";
        String params = "Test_OBSERVE" + observed + "obInterval" + obInterval + "_numSamples" + numSamples + "_numTimeSteps" + numTimeSteps + "_numRandomDoubles" + numRandomDoubles + "_totalNumPeople" + totalNumPeople + "_dropSamples" + dropSamples + "_downSample" + "_sigmaNoise" + sigmaNoise + "_downsample" + downSample + "_timeStamp" + System.currentTimeMillis();

        // Write out samples
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Samples_" + params + ".csv"),
                "utf-8"));
            for (int i = 0; i < samples.size(); i++) {
                Integer[] peoplePerIter = samples.get(i);
                System.out.println("length: " + peoplePerIter);
                for (int j = 0; j <  peoplePerIter.length ; j++) {
                    if ((j + 1) % numOutputs == 0) {
                        writer.write(peoplePerIter[j] + "");
                        if (j < peoplePerIter.length - numOutputs) {
                            System.out.println(j);
                            writer.write(",");
                        }
                    }
                }
                writer.write(System.lineSeparator());
            }
        } catch (IOException ex) {
            System.out.println("Error writing to file");
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                System.out.println("Error closing file");
            }
        }

        // Write out Truth
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Truth_" + params + ".csv"),
                "utf-8"));
            for (int i = 0; i < truth.length ; i++) {
                if ((i + 1) % numOutputs == 0) {
                    writer.write(truth[i] + "");
                    if (i < truth.length - numOutputs) {
                        writer.write(",");
                    }
                }
            }
            writer.write(System.lineSeparator());

        } catch (IOException ex) {
            System.out.println("Error writing to file");
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
                System.out.println("Error closing file");
            }
        }
    }


    public static Integer[] run(RandomGenerator rand) {
        System.out.println("Model "+ Station.modelCount++ +" starting");
        stationSim.start(rand);
        int numOutputs = stationSim.getNumEntrances() + stationSim.getNumExits() + 1;
        System.out.println("Num of outputs: " + numOutputs);

        Integer[] stepOutput;
        Integer[] results = new Integer[numTimeSteps * numOutputs + 1];
        for (int i = 0; i < results.length; i++) {
            results[i] = 0;
        }
        //System.out.println("length of results: " + results.length);
        int i = 0;
        do {
            // Run a step of each simulation
            if (!stationSim.schedule.step(stationSim)) {
                break;
            }
            stepOutput = stationSim.analysis.getNumPeopleInandOut();
            //System.out.println(stepOutput.length);
            for (int j = 0; j < numOutputs; j++) {
                //System.out.println(i + j);
                results[i * numOutputs + j] = stepOutput[j];
                //System.out.println("j: " + j);
            }
            //System.out.println(stepOutput[5]);
            i++;
            //System.out.println("i: " + i);
        } while (stationSim.area.getAllObjects().size() > 0 && i < numTimeSteps);
        stationSim.finish();
        //      results.add(Arrays.asList(numPeople));

        return results;
    }

    public static List<Integer[]> keanu(Integer[] truth, boolean observe, int obInterval) {

        System.out.println("Initialising random number stream");

        //VertexBackedRandomFactory random = new VertexBackedRandomFactory(numInputs,, 0, 0);
        RandomFactoryVertex random = new RandomFactoryVertex (numRandomDoubles, 0, 0);


        // This is the 'black box' vertex that runs the model. It's input is the random numbers and
        // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
        //BlackBox box = new BlackBox(inputs, wrap::run, Wrapper.numTimeSteps);
        //UnaryOpVertex<RandomFactory,Integer[]> box = new Unar<>( random, wrap::run )
        System.out.println("Initialising black box model");
        UnaryOpLambda<VertexBackedRandomGenerator,Integer[]> box = new UnaryOpLambda<>( random, WrapperTest::run);

        // This is the list of random numbers that are fed into model (similar to drawing from a distribution,
        // but they're pre-defined in randSource)



        System.out.println("Before:\n Mu");
        List<GaussianVertex>  randSource = random.getValue().randDoubleSource;
        for (GaussianVertex num : randSource) {
            System.out.print(num.getMu().getValue().scalar() + ",");
        }
        System.out.println("Sigma");
        for (GaussianVertex num : randSource) {
            System.out.print(num.getSigma().getValue().scalar() + ",");
        }

        // Observe the truth data plus some noise?
        if (observe) {
            System.out.println("Observing truth data. Adding noise with standard dev: " + sigmaNoise);
            for (Integer i = 0; i < truth.length; i++) {
                if((i + 1) % 6 != 0) {
                    //if (i % 6 != 0) {
                    // output is the ith element of the model output (from box)
                    IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
                    // output with a bit of noise. Lower sigma makes it more constrained.
                    GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), sigmaNoise);
                    // Observe the output
                    System.out.println(truth[i]);
                    noisyOutput.observe(truth[i].doubleValue()); //.toDouble().scalar());
                    //}
                }
            }
        }
        else {
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

        System.out.println("After:\nMu");
        randSource = random.getValue().randDoubleSource;
        for (GaussianVertex num : randSource) {
            System.out.print(num.getMu().getValue().scalar() + ",");
        }
        System.out.println("Sigma");
        for (GaussianVertex num : randSource) {
            System.out.print(num.getSigma().getValue().scalar() + ",");
        }

        return samples;
    }

    public static void main(String[] args) {
        List<Integer[]> samples;
        Boolean observe;

        System.out.println("Starting. Number of iterations: " + numTimeSteps);

        // Make truth data
        System.out.println("Making truth data");
        VertexBackedRandomGenerator truthRandom = new VertexBackedRandomGenerator(numRandomDoubles, 0, 0);
        Integer[] truth = WrapperTest.run(truthRandom);

        System.out.println("Random values - Truth:\nMu");

        List<GaussianVertex>  randSource = truthRandom.randDoubleSource;
        for (GaussianVertex num : randSource) {
            System.out.print(num.getMu().getValue().scalar() + ",");
        }
        System.out.println("Sigma");
        for (GaussianVertex num : randSource) {
            System.out.print(num.getSigma().getValue().scalar() + ",");
        }

        // Results without observations of truth data
        observe = false;
        samples = keanu(truth, observe, 0);
        writeResults(samples, truth, observe, 0);

        //int[] obIntervals = {1,5,10,50,100};
        int[] obIntervals = {1};
        for(int i = 0; i < obIntervals.length; i++) {
            observe = true;
            samples = keanu(truth, observe, obIntervals[i]);
            writeResults(samples, truth, observe, obIntervals[i]);
        }
    }
}