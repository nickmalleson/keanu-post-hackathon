package io.improbable.keanu.ABM;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

public class Simulation extends SimState
{
    public static Simulation state;

    public final double yardSize = 180.0;
    public Continuous2D yard = new Continuous2D(1.0, yardSize, yardSize);


    public Simulation(long seed)
    {
        super(seed);
        state = this;
    }


    public void start()
    {
        super.start();
        yard.clear();

        // add agents to the yard
//        spawnGrid(Prey.class, new Double2D(0,0), new Double2D(yardSize-1, 5));
//        spawnGrid(Predator.class, new Double2D(0,3), new Double2D(yardSize-1, 3));
        spawnRandomPosition(Prey.class, (int)(0.08*yardSize*yardSize/(Agent.moveSpeed*Agent.moveSpeed)));
        spawnRandomPosition(Predator.class, (int)(0.02*yardSize*yardSize/(Agent.moveSpeed*Agent.moveSpeed)));
    }


    public void spawnGrid(Class<? extends Agent> agentType, Double2D topLeft, Double2D bottomRight) {
        try {
            MutableDouble2D spawnPos = new MutableDouble2D(topLeft);
            for(spawnPos.y = topLeft.y; spawnPos.y <= bottomRight.y; spawnPos.y++) {
                for(spawnPos.x = topLeft.x; spawnPos.x <= bottomRight.x; spawnPos.x++) {
                    agentType.newInstance().setPos(new Double2D(spawnPos));
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public void spawnRandomPosition(Class<? extends Agent> agentType, int numberToSpawn) {
        try {
            while (numberToSpawn > 0) {
                agentType.newInstance().setPos(new Double2D(random.nextDouble()*yardSize, random.nextDouble()*yardSize));
                numberToSpawn--;
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        doLoop(Simulation.class, args);
        System.exit(0);
    }
}
