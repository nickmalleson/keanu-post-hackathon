package StationSim;



import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.research.BlackBox;
import io.improbable.keanu.research.randomfactory.RandomFactory;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.research.vertices.RandomFactoryVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpVertex;
import sun.nio.ch.Net;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nick on 22/06/2018.
 */
public class Wrapper {

    static Station stationSim = new Station(System.currentTimeMillis());
    private static int capacity = 10000;

    public Wrapper() {

    }

    public static Integer[] run(RandomFactory rand) {
        stationSim.start(rand);

        Integer[] numPeople = new Integer[capacity];
        for (int i = 0; i < capacity; i++) {
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
        } while (stationSim.area.getAllObjects().size() > 0 && i < capacity);

        return numPeople;
    }

    public static void main(String[] args) {
        Wrapper wrap = new Wrapper();
        //VertexBackedRandomFactory random = new VertexBackedRandomFactory(200, 0, 0);
        RandomFactoryVertex random = new RandomFactoryVertex (200, 0, 0);

        ArrayList<DoubleVertex> inputs = new ArrayList<>(0);


        //BlackBox box = new BlackBox(inputs, wrap::run, Wrapper.capacity);
        //UnaryOpVertex<RandomFactory,Integer[]> box = new Unar<>( random, wrap::run )
        UnaryOpLambda<VertexBackedRandomFactory,Integer[]> box = new UnaryOpLambda<>( random, Wrapper::run);

        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        System.out.println(testNet.getLatentVertices().size());

        NetworkSamples sampler = MetropolisHastings.getPosteriorSamples( testNet, Arrays.asList(box),100 );


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
