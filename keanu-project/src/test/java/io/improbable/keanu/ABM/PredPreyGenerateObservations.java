package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;

public class PredPreyGenerateObservations {

    public static void main (String[] args) {


        Simulation simulation = new Simulation(10, 10, new VertexBackedRandomFactory(100, 100), 100,
            20, 5, 0.02, 0.06, 0.03);

        simulation.initialiseSimulation();
        simulation.run();

        System.out.println("Final number of Prey: " + simulation.numberOfPrey);
        System.out.println("Final number of Pred: " + simulation.numberOfPredators);
    }
}
