package SimpleModel;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.nd4j.linalg.api.rng.DefaultRandom;

import java.util.ArrayList;

public class SimpleModel {

    /* Parameters */
    private double threshold = 0.5;
    private long SEED = 1l;

    /* Variables */
    private int counter = 0;
    private ArrayList<Integer> history = new ArrayList<Integer>();
    private RandomGenerator random = null;


    /* Constructors */

    public SimpleModel( ) {
        this.random = new MersenneTwister(SEED);
    }


    /* Methods */

    public void step() {
        this.counter = this.random.nextDouble() < this.threshold ? this.counter+1 : this.counter-1;
        this.history.add(this.counter);
    }


    /* Getters & Setters */

    public int getCounter() {
        return this.counter;
    }

    public ArrayList<Integer> getHistory() {
        return this.history;
    }



    /* Main */
    
    public static void main(String args[]) {

        SimpleModel s = new SimpleModel();
        for (int i=0; i<1000; i++ ) {
            s.step();
        }
        for (int c: s.history) {
            System.out.print(c+", ");
        }
        System.out.println();

    }

}
