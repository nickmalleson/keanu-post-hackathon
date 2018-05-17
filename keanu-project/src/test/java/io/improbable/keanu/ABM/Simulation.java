package io.improbable.keanu.ABM;

import io.improbable.keanu.research.VertexBackedRandomFactory;

import java.util.ArrayList;

public class Simulation {

    Agent[][] grid;
    VertexBackedRandomFactory random;

    public Simulation(int XSIZE, int YSIZE, VertexBackedRandomFactory random) {
        grid = new Agent[XSIZE][YSIZE];
        this.random = random;
    }

    public void step() {
        ArrayList<Agent> tempAgentCollection = new ArrayList<>();
        for (Agent[] subset: grid) {
            for (Agent agent: subset) {
                tempAgentCollection.add(agent);
            }
        }
        for (Agent agent: tempAgentCollection) {
            agent.step();
        }
    }

    public void run(int noSteps) {
        for (int i=0; i<noSteps; i++) {
            step();
        }
    }

    public Agent getXY(int xLocation, int yLocation) {
        return grid[(xLocation+grid.length)%grid.length][(yLocation+grid[0].length)%grid[0].length];
    }
}