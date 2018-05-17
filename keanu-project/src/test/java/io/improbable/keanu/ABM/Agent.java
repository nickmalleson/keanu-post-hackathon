package io.improbable.keanu.ABM;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

/**
 * Created by daniel on 10/04/17.
 */
public class Agent implements Steppable {
    static final double moveSpeed = 1.0;

    public Stoppable swoff;
    MutableDouble2D myPos;


    public Agent()
    {
        swoff=Simulation.state.schedule.scheduleRepeating(this, 2, 1.0);
        myPos = new MutableDouble2D();
    }


    public void step(SimState state)
    {
        //Movement
        double yardSize = Simulation.state.yardSize;
        setPos(new Double2D(
                ((myPos.x + state.random.nextGaussian()*moveSpeed) + yardSize)%yardSize,
                ((myPos.y + state.random.nextGaussian()*moveSpeed) + yardSize)%yardSize)
        );
    }


    public void setPos(Double2D pos) {
        myPos.setTo(pos);
        Simulation.state.yard.setObjectLocation(this, pos);
    }


    public void remove() {
        swoff.stop();
        Simulation.state.yard.remove(this);
    }


    void setPos(MutableDouble2D pos) {
        setPos(new Double2D(pos));
    }
}
