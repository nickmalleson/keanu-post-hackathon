package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class Agent {

    Simulation sim;
    private int xLocation;
    private int yLocation;
    VertexBackedRandomFactory random;
    ArrayList<Agent> proximateAgents;

    Agent(Simulation sim, int startX, int startY) {
        this.sim = sim;
        this.xLocation = startX;
        this.yLocation = startY;
        random = sim.random.nextRandomFactory();

    }

    public void step() {

        Integer direction = random.nextDouble(0, 4).intValue();

        int testYLocation = yLocation;
        int testXLocation = xLocation;

        switch (direction) {
            case 0:
                testYLocation += 1;
                if (testYLocation > sim.grid[0].length - 1) { testYLocation = 0; }
                break;
            case 1:
                testXLocation += 1;
                if (testXLocation > sim.grid.length - 1) { testXLocation = 0; }
                break;
            case 2:
                testYLocation -= 1;
                if (testYLocation < 0) { testYLocation = sim.grid[0].length - 1; }
                break;
            case 3:
                testXLocation -= 1;
                if (testXLocation < 0) { testXLocation = sim.grid.length - 1; }
                break;
        }

        if (sim.grid[testXLocation][testYLocation] == null) {
            sim.grid[xLocation][yLocation] = null;
            xLocation = testXLocation;
            yLocation = testYLocation;
            sim.grid[xLocation][yLocation] = this;
        }

        proximateAgents();
    }

    private void proximateAgents() {
        ArrayList<Agent> proximateAgents = new ArrayList<>();
        for (int i=xLocation-1; i<=xLocation+1; i++) {
            for (int j=yLocation-1; j<=yLocation+1; j++) {
                proximateAgents.add(sim.getXY(i, j));
            }
        }
        proximateAgents.remove(this);
        this.proximateAgents = proximateAgents;
    }

    long getNumberOfProximatePrey() {
        return proximateAgents.stream().filter((Agent i) -> i instanceof Prey).count();
    }

    long getNumberOfProximatePredators() {
        return proximateAgents.stream().filter((Agent i) -> i instanceof Predator).count();
    }

    public void removeAgent() {
        // TODO does this work?
        sim.grid[xLocation][yLocation] = null;
    }

    void giveBirth(BiConsumer<Integer, Integer> function) {
        boolean pregnant = true;
        while (pregnant) {
            for (int i = xLocation - 1; i <= xLocation + 1; i++) {
                for (int j = yLocation - 1; j <= yLocation + 1; j++) {
                    if (sim.getXY(i, j) == null) {
                        function.accept(i, j);
                        pregnant = false;
                    }
                }
            }
            pregnant = false;
        }
    }


}
