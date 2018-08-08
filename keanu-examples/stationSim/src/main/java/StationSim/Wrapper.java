package StationSim;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.research.visualisation.GraphvizKt;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nick on 22/06/2018.
 */
public class Wrapper{

    private static int numTimeSteps = 2000;
    public static int numRandomDoubles = 10;
    private static int numSamples = 500;
    private static int dropSamples = 200;
    private static int downSample = 3;
    private static double sigmaNoise = 0.1; // The amount of noise to be added to the truth

    private static boolean justCreateGraphs = false; // Create graphs and then exit, no sampling

    private static String dirName = "results/"; // Place to store results

    public static void writeResults(List<Integer[]> samples, Integer[] truth, String params) {
        Writer writer = null;

        // Write out samples
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Samples_" + params + ".csv"),
                "utf-8"));
            System.out.println("writing: " + dirName + "Samples_" + params + ".csv");
            for (int i = 0; i < samples.size(); i++) {
                Integer[] peoplePerIter = samples.get(i);
                for (int j = 0; j <  peoplePerIter.length ; j++) {
                    writer.write(peoplePerIter[j] + "");
                    if (j != peoplePerIter.length - 1) {
                        writer.write(",");
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
                writer.write(truth[i] + "");
                if (i != truth.length - 1) {
                    writer.write(",");
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

        Station stationSim = new Station(System.currentTimeMillis());
        System.out.println("Model "+ Station.modelCount++ +" starting");
        stationSim.start(rand);

        Integer[] numPeople = new Integer[numTimeSteps];
        for (int i = 0; i < numTimeSteps; i++) {
            numPeople[i] = 0;
        }

        int i = 0;
        do {
            // Run a step of each simulation
            if (!stationSim.schedule.step(stationSim)) {
                break;
            }
            numPeople[i] = stationSim.area.getAllObjects().size();
            i++;
        } while (stationSim.area.getAllObjects().size() > 0 && i < numTimeSteps);
        stationSim.finish();

        //      results.add(Arrays.asList(numPeople));

        return numPeople;
    }

    public static List<Integer[]> keanu(Integer[] truth, int obInterval, long timestamp, boolean createGraph) {

        // (Useful string for writing results)
        int totalNumPeople = new Station(System.currentTimeMillis()).getNumPeople();
        String params = "obInterval" + obInterval + "_numSamples" + numSamples + "_numTimeSteps" + numTimeSteps + "_numRandomDoubles" + numRandomDoubles + "_totalNumPeople" + totalNumPeople + "_dropSamples" + dropSamples + "_downSample" + "_sigmaNoise" + sigmaNoise + "_downsample" + downSample + "_timeStamp" + timestamp;

        System.out.println("Initialising random number stream");
        //VertexBackedRandomFactory random = new VertexBackedRandomFactory(numInputs,, 0, 0);
        RandomFactoryVertex random = new RandomFactoryVertex (numRandomDoubles, 0, 0);

        // This is the 'black box' vertex that runs the model. It's input is the random numbers and
        // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
        System.out.println("Initialising black box model");
        UnaryOpLambda<VertexBackedRandomGenerator,Integer[]> box = new UnaryOpLambda<>( random, Wrapper::run);

        // Observe the truth data plus some noise?
        if (obInterval > 0) {
            System.out.println("Observing truth data. Adding noise with standard dev: " + sigmaNoise);
            for (Integer i = 0; i < numTimeSteps; i++) {
                if(i % obInterval == 0) {
                    // output is the ith element of the model output (from box)
                    IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
                    // output with a bit of noise. Lower sigma makes it more constrained.
                    GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), sigmaNoise);
                    // Observe the output
                    noisyOutput.observe(truth[i].doubleValue()); //.toDouble().scalar());
                }
            }
        }
        else {
            System.out.println("Not observing truth data");
        }

        System.out.println("Creating BayesNet");
        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        // Create a graph and write it out
        // Write out samples
        try {
            System.out.println("Writing out graph");
            Writer graphWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(dirName + "Graph_" + params + ".dot"),
                "utf-8"));
            graphWriter.write(GraphvizKt.toGraphvizString(testNet, new HashMap<>()) );
            graphWriter.close();
       } catch (IOException ex) {
            System.out.println("Error writing graph to file");
        }

        // If just creating a graph then don't do anything further
        if (createGraph) {
            System.out.println("\n\n\n" + GraphvizKt.toGraphvizString(testNet, new HashMap<>()) + "\n\n\n");
            System.out.println("Have created graph. Not sampling");
            return new ArrayList<Integer[]>();
        }

        // Workaround for too many evaluations during sample startup
        random.setAndCascade(random.getValue());

        // Sample: feed each randomNumber in and run the model
        System.out.println("Sampling");
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples(testNet, Arrays.asList(box), numSamples);

        // Interrogate the samples

        // Get the number of people per iteration (an array of IntegerTensors) for each sample
        //List<Integer[]> samples = sampler.drop(dropSamples).downSample(downSample).get(box).asList();
        List<Integer[]> samples = sampler.get(box).asList(); // temporarily not dropping samples

        writeResults(samples, truth, params);

        return samples;
    }

    public static void main(String[] args) {

        long timestamp = System.currentTimeMillis();

        System.out.println("Starting. Number of iterations: " + numTimeSteps);

        // Make truth data
        System.out.println("Making truth data");
        VertexBackedRandomGenerator truthRandom = new VertexBackedRandomGenerator(numRandomDoubles, 0, 0);
        Integer[] truth = Wrapper.run(truthRandom);

        //Run kenanu
        ArrayList<Integer> obIntervals = new ArrayList<>(Arrays.asList(0,1));
        obIntervals.parallelStream().forEach(i -> keanu(truth, i, timestamp, justCreateGraphs));

    }
}
