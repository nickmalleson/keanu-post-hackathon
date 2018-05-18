package io.improbable.keanu.ABM;

public class Predator extends Agent {

    private boolean hasEaten = false;

    Predator(Simulation sim, int startX, int startY) {
        super(sim, startX, startY);
    }

    public void step() {
        super.step();
        hunt();
        controlPopulation();
    }

    private void hunt() {
        for (Agent agent: proximateAgents) {
            if (agent instanceof Prey) {
                agent.removeAgent();
                hasEaten = true;
            }
        }
    }

    private void controlPopulation() {
        if (!hasEaten && random.nextDouble(0, 240) < 1) {
            removeAgent();
        } else if (hasEaten && random.nextDouble(0, 1) > (0.03 * getNumberOfProximatePredators())) {
            giveBirth(sim::spawnPreditor);
        }
    }
}
