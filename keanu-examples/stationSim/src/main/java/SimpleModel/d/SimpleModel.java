package SimpleModel.d;

import org.apache.commons.math3.random.RandomGenerator;

public class SimpleModel {

    /* Class Parameters */
    public double threshold;

    public RandomGenerator random;

    /** Initialise the class (only needs to be called once) */
    public SimpleModel(double threshold, RandomGenerator random) {
        this.threshold = threshold;
        this.random = random;
    }

    /**
     * Step the model by one iteration.
     * @param state The current state of the model.
     * @return The new state after one iteration
     */
    public int step(int state) {
        int newState = this.random.nextGaussian() > this.threshold ? state+1 : state-1;
        return newState;
    }

    /**
     * Step the model by a number of iterations
     * @param state The current state of the model.
     * @param iter The number of iterations to step the model. Must be > 0
     * @return The new state after <code>iter</code> iterations
     */
  /**  public final int step(int state, int iter) {
        assert iter > 0;

        int currentState = state;
        for (int i=0; i< iter; i++) {
            int newState = step(currentState);
            currentState = newState;
        }
        return currentState;
    } */



}
