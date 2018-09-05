package SimpleModel;

import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.nd4j.linalg.api.rng.DefaultRandom;

import java.util.ArrayList;

public class SimpleModel {

    /* Parameters */
    private double threshold;
    private RandomGenerator random;

    /* Variables */
    private int counter = 0;
    private ArrayList<Integer> history = new ArrayList<Integer>();



    /* Constructors */

    public SimpleModel(double threshold, RandomGenerator random ) {
        this.threshold = threshold;
        this.random = random;
    }


    /* Methods */

    /**
     * Adds or subtracts 1 from the counter depending on the value of a random draw.
     */
    public void step() {
        this.counter = this.random.nextDouble() < this.threshold ? this.counter+1 : this.counter-1;
        this.history.add(this.counter);
    }


    /* Getters & Setters etc .*/

    public int getCounter() {
        return this.counter;
    }

    public Integer[] getHistory() {
        return this.history.toArray(new Integer[this.history.size()]);
    }

    public void printHistory() {
        for (int c: this.history) {
            System.out.print(c+", ");
        }
        System.out.println();
    }


    /* Main */

    public static void main(String args[]) {

        SimpleModel s = new SimpleModel(0.5, new MersenneTwister(1l));
        for (int i=0; i<1000; i++ ) {
            s.step();
        }
        s.printHistory();

    }

}
