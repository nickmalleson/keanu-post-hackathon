package StationSim;



import io.improbable.keanu.algorithms.NetworkSamples;
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
    private static int numTimeSteps = 500;

    static ArrayList<List<IntegerTensor>> results = new ArrayList<List<IntegerTensor>>();

    public Wrapper() {

    }

    public static IntegerTensor[] run(RandomFactory rand) {
        stationSim.start(rand);

        IntegerTensor[] numPeople = new IntegerTensor[numTimeSteps];
        for (int i = 0; i < numTimeSteps; i++) {
            numPeople[i] = IntegerTensor.scalar(0);
        }

        int i = 0;
        do {
            // Run a step of each simulation
            if (!stationSim.schedule.step(stationSim)) {
                break;
            }
            numPeople[i].setValue(stationSim.area.getAllObjects().size());
            i++;
        } while (stationSim.area.getAllObjects().size() > 0 && i < numTimeSteps);

        results.add(Arrays.asList(numPeople));

        return numPeople;
    }

    public static void main(String[] args) {

        // Make truth data
        IntegerTensor[] truth = Wrapper.run(new VertexBackedRandomFactory(200, 0, 0));

        Wrapper wrap = new Wrapper();
        //VertexBackedRandomFactory random = new VertexBackedRandomFactory(200, 0, 0);
        RandomFactoryVertex random = new RandomFactoryVertex (200, 0, 0);

        ArrayList<DoubleVertex> inputs = new ArrayList<>(0);


        // This is the 'black box' vertext that runs the model. It's input is the random numbers and
        // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
        //BlackBox box = new BlackBox(inputs, wrap::run, Wrapper.numTimeSteps);
        //UnaryOpVertex<RandomFactory,Integer[]> box = new Unar<>( random, wrap::run )
        UnaryOpLambda<VertexBackedRandomFactory,IntegerTensor[]> box = new UnaryOpLambda<>( random, Wrapper::run);

        // This is the list of random numbers that are fed into model (similar to drawing from a distribution,
        // but they're pre-defined in randSource)
        DoubleVertex randSource  = random.getValue().randDoubleSource;


        // Observe the truth data plus some noise
        for (int i = 0; i< numTimeSteps; i++) {
            // output is the ith element of the model output (from box)
            IntegerArrayIndexingVertex output = new IntegerArrayIndexingVertex(box, i);
            // output with a bit of noise
            GaussianVertex noiseyOutput = new GaussianVertex(new CastDoubleVertex(output), 1.0);
            // Observe the output
            noiseyOutput.observe(truth[i].scalar().doubleValue()); //.toDouble().scalar());
        }

        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        // Sample: feed each randomNumber in and run the model
        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples( testNet, Arrays.asList(randSource, box), 10 );

        //System.out.println(testNet.getLatentVertices().size());

        // XXXX To get the num agents at each iteration now: feed the random numbers back in to the model
        System.out.println(results.size());
        System.out.println(results.get(0).size());


        // print the first random draw of the first sample
        System.out.println(sampler.get(randSource).asList().get(0).getValue(0));

        // Number of people in zeroth iteration of zeroth sample
        System.out.println(sampler.get(box).asList().get(0)[0].scalar());

        //for (int i=0; i<sampler.get(box).asList().size() ; i++) {
        //    System.out.println(sampler.get(box).asList().get(i)[0].scalar());
        //}

        // Number of people at each iteration in first sample
        for (int i=0; i<sampler.get(box).asList().get(0).length ; i++) {
            System.out.println(sampler.get(box).asList().get(0)[i].scalar());
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
