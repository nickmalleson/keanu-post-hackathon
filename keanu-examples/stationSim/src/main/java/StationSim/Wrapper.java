package StationSim;



import io.improbable.keanu.research.randomfactory.RandomFactory;
import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 22/06/2018.
 */
public class Wrapper {

    Station stationSim = new Station(System.currentTimeMillis());

    public Wrapper() {

    }

    public Integer[] run(RandomFactory rand) {
        stationSim.start(rand);
        int capacity = 10000;
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
        VertexBackedRandomFactory random = new VertexBackedRandomFactory(10000, 0, 0);
        Integer[] numPeople = wrap.run(random);
        for (Integer n : numPeople) {
            //System.out.println(n);
        }
        System.out.println(random.gaussianCounter);
    }


}
