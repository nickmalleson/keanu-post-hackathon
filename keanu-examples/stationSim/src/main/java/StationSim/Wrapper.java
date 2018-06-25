package StationSim;



import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.VertexSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.randomfactory.RandomFactory;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.tensor.intgr.IntegerTensor;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nick on 22/06/2018.
 */
public class Wrapper {

    static Station stationSim = new Station(System.currentTimeMillis());
    private static int numTimeSteps = 1000;
    public static int numRandomDoubles = 200;


//    static ArrayList<List<IntegerTensor>> results = new ArrayList<List<IntegerTensor>>();

    public Wrapper() {

    }

    public static Integer[] run(RandomFactory rand) {
        System.out.println("Model "+Station.modelCount++ +" starting");
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

  //      results.add(Arrays.asList(numPeople));

        return numPeople;
    }

    public static void main(String[] args) {

        System.out.println("Starting. Number of iterations: "+numTimeSteps);

        // Make truth data
        System.out.println("Making truth data");
        Integer[] truth = Wrapper.run(new VertexBackedRandomFactory(0, 0, 0));

        System.out.println("Initialising random number stream");
        Wrapper wrap = new Wrapper();
        //VertexBackedRandomFactory random = new VertexBackedRandomFactory(numInputs,, 0, 0);
        RandomFactoryVertex random = new RandomFactoryVertex (numRandomDoubles, 0, 0);

        ArrayList<DoubleVertex> inputs = new ArrayList<>(0);


        // This is the 'black box' vertext that runs the model. It's input is the random numbers and
        // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
        //BlackBox box = new BlackBox(inputs, wrap::run, Wrapper.numTimeSteps);
        //UnaryOpVertex<RandomFactory,Integer[]> box = new Unar<>( random, wrap::run )
        System.out.println("Initialising black box model");
        UnaryOpLambda<VertexBackedRandomFactory,Integer[]> box = new UnaryOpLambda<>( random, Wrapper::run);

        // This is the list of random numbers that are fed into model (similar to drawing from a distribution,
        // but they're pre-defined in randSource)
        List<GaussianVertex> randSource  = random.getValue().randDoubleSource;


        // Observe the truth data plus some noise
        System.out.println("Observing truth data");
        for (Integer i = 0; i< numTimeSteps; i++) {
            // output is the ith element of the model output (from box)
            IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
            // output with a bit of noise
            GaussianVertex noisyOutput = new GaussianVertex(new CastDoubleVertex(output), 1.0);
            // Observe the output
            noisyOutput.observe(truth[i].doubleValue()); //.toDouble().scalar());
        }

        System.out.println("Creating BayesNet");
        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        // Workaround for too many evaluations during sample startup
        random.setAndCascade(random.getValue());

        // Sample: feed each randomNumber in and run the model
        System.out.println("Sampling");
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples( testNet, Arrays.asList(box), 20 );

        // Interrogate the samples

        // Get the number of people per iteration (an array of IntegerTensors) for each sample
        List<Integer[]> l = sampler.get(box).asList();

        // Print Number of people at each iteration in every sample

        for (int i=0; i<l.size(); i++) {
            System.out.print("Sample "+i+": ");

            Integer[] peoplePerIter = l.get(i);

            for (int j=0; j<peoplePerIter.length ; j++) {
                System.out.print(peoplePerIter[j]+",");
            }

            System.out.println("");
        }






//        NonGradientOptimizer optimizer = new NonGradientOptimizer(testNet);

        //optimizer.maxLikelihood(10, 1000);

  //      optimizer.maxAPosteriori(1000000, 10.0);


        //Integer[] numPeople = wrap.run(random);
        //for (Integer n : numPeople) {
            //System.out.println(n);
        //}
        //System.out.println(random.gaussianCounter);
    }


}
