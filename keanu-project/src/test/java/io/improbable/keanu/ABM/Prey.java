package io.improbable.keanu.ABM;

public class Prey extends Agent
{
    public static int nPrey = 0;


    public Prey() {
        nPrey++;
    }


    public void step(SimState state)
    {
        super.step(state);
        Simulation simulation = (Simulation) state;
        int preys=0;

        //Overpopulation part
        Bag cell_population=simulation.yard.getNeighborsExactlyWithinDistance(new Double2D(myPos),moveSpeed*4.0/3.0);
        for(Object individual : cell_population) {
            if (individual instanceof Prey) {
                preys++;
            }
        }
        if(preys > 5 && simulation.random.nextDouble() < 0.5) {
            remove();
        }

        //Reproduction part
        if(simulation.random.nextDouble() < (0.02*preys)+0.06) {
            Prey prey = new Prey();
            prey.setPos(myPos);
        }
    }


    @Override
    public void remove() {
        super.remove();
        nPrey--;
    }

}
