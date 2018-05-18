package io.improbable.keanu.ABM;

public class Prey extends Agent {

    Prey(Simulation sim, int startX, int startY) {
        super(sim, startX, startY);
    }

    public void step() {
        super.step();
        long proximatePrey = getNumberOfProximatePrey();
        controlPopulation(proximatePrey);
    }

    private void controlPopulation(long proximatePrey) {

        if (proximatePrey > 5 && random.nextDouble(0,2) > 1) {
            removeAgent();
        } else if (random.nextDouble(0, 1) < (0.02 * proximatePrey) + 0.06) {
            giveBirth(sim::spawnPrey);
        }
    }
}
