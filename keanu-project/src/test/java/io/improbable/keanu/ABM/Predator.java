package io.improbable.keanu.ABM;

import sim.engine.*;
import sim.util.*;

public class Predator extends Agent {
    public static int nPredators;

    public Predator() {
        nPredators++;
    }

    public void step(SimState state) {
        super.step(state);
        Simulation simulation = (Simulation) state;
        int preds = 0;
        boolean haveEaten;

        //Hunting part
        Bag cell_population = simulation.yard.getNeighborsExactlyWithinDistance(new Double2D(myPos), moveSpeed*4.0/3.0);
        haveEaten = false;
        for (int i = 0; i < cell_population.size(); i++) {
            Object individual = cell_population.get(i);
            if (individual instanceof Predator) {
                preds += 1;
            } else if (!haveEaten && individual instanceof Prey) {
                ((Prey) individual).remove();
                haveEaten = true;
            }
        }
        //Reproduction part
        if (haveEaten == true && simulation.random.nextDouble() < (0.03 * preds)) {
            Predator predator = new Predator();
            predator.setPos(myPos);
        }
        //Starvation part
        if (haveEaten == false && simulation.random.nextDouble() < 1.0 / 240.0) {
            this.remove();
        }
    }

    @Override
    public void remove() {
        super.remove();
        nPredators--;
    }
}