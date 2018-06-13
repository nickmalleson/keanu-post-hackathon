package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class Simulation {

    Agent[][] grid;
    VertexBackedRandomFactory random;

    private Integer timesteps;
    Integer numberOfPredators;
    Integer numberOfPrey;
    private Double preyReproductionGradient;
    private Double preyReproductionConstant;
    private Double predReproductionGradient;

    public Simulation(int XSIZE, int YSIZE, VertexBackedRandomFactory random, Integer timesteps,
                      Integer initialNumberOfPrey, Integer initialNumberOfPredators,
                      Double preyReproductionGradient, Double preyReproductionConstant,
                      Double predReproductionGradient) {
        this.timesteps = timesteps;
        this.numberOfPrey = initialNumberOfPrey;
        this.numberOfPredators = initialNumberOfPredators;
        this.preyReproductionGradient = preyReproductionGradient;
        this.preyReproductionConstant = preyReproductionConstant;
        this.predReproductionGradient = predReproductionGradient;

        grid = new Agent[XSIZE][YSIZE];
        this.random = random;
    }

    public void initialiseSimulation() {
        randomSpawnPopulation(numberOfPredators, this::spawnPreditor);
        randomSpawnPopulation(numberOfPrey, this::spawnPrey);
        System.out.println("Simulation initialised");
    }

    private void step() {
        ArrayList<Agent> tempAgentCollection = new ArrayList<>();
        for (Agent[] subset: grid) {
            for (Agent agent: subset) {
                if (agent != null) {
                    tempAgentCollection.add(agent);
                }
            }
        }
        for (Agent agent: tempAgentCollection) {
            agent.step();
        }
    }

    public void run() {
        for (int i=0; i<timesteps; i++) {
            step();
        }
    }

    private void randomSpawnPopulation(Integer numberToSpawn, BiConsumer<Integer, Integer> function) {
        int i = 0;
        while (i < numberToSpawn) {
            int proposedX = random.nextDouble(0, grid.length).intValue();
            int proposedY = random.nextDouble(0, grid[0].length).intValue();
            if (getXY(proposedX, proposedY) == null) {
                function.accept(proposedX, proposedY);
                i++;
            }
        }
    }

    public Agent getXY(int xLocation, int yLocation) {
        return grid[(xLocation+grid.length)%grid.length][(yLocation+grid[0].length)%grid[0].length];
    }

    public void spawnPrey(int startX, int startY) {
        grid[startX][startY] = new Prey(this, startX, startY, preyReproductionGradient, preyReproductionConstant);
    }

    public void spawnPreditor(int startX, int startY) {
        grid[startX][startY] = new Predator(this, startX, startY, predReproductionGradient);
    }
}
