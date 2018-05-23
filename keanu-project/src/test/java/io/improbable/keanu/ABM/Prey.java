package io.improbable.keanu.ABM;

public class Prey extends Agent {

    private final Double preyReproductionGradient; // 0.02
    private final Double preyReproductionConstant; // 0.06

    Prey(Simulation sim, int startX, int startY, Double preyReproductionGradient, Double preyReproductionConstant) {
        super(sim, startX, startY);
        this.preyReproductionGradient = preyReproductionGradient;
        this.preyReproductionConstant = preyReproductionConstant;
    }

    public void step() {
        super.step();
        long proximatePrey = getNumberOfProximatePrey();
        controlPopulation(proximatePrey);
    }

    private void controlPopulation(long proximatePrey) {

        if (proximatePrey > 5 && random.nextDouble(0,2) > 1) {
            removeAgent();
        } else if (random.nextDouble(0, 1) < (preyReproductionGradient * proximatePrey) + preyReproductionConstant) {
            giveBirth(sim::spawnPrey);
        }
    }
}
