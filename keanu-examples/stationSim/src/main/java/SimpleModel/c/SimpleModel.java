package SimpleModel.c;

import org.apache.commons.math3.random.RandomGenerator;
import sun.jvm.hotspot.utilities.AssertionFailure;

public abstract class SimpleModel {

    /* Class Parameters */
    private static double threshold;



    private static RandomGenerator random;
    private static boolean init = false; // Check that the class has been initialised

    /** Initialise the class (only needs to be called once) */
    public static void init(double threshold, RandomGenerator random) {
        SimpleModel.threshold = threshold;
        SimpleModel.random = random;
        init=true;
    }

    /**
     * Step the model by one iteration.
     * @param state The current state of the model.
     * @return The new state after one iteration
     */
    public static final int step(int state) {
        if (!init) throw new AssertionError("SimpleModel class has not been initialised. Call init().");
        int newState = SimpleModel.random.nextGaussian() > SimpleModel.threshold ? state+1 : state-1;
        return newState;
    }

    /**
     * Step the model by a number of iterations
     * @param state The current state of the model.
     * @param iter The number of iterations to step the model. Must be > 0
     * @return The new state after <code>iter</code> iterations
     */
    public static final int step(int state, int iter) {
        if (!init) throw new AssertionError("SimpleModel class has not been initialised. Call init().");
        assert iter > 0;

        int currentState = state;
        for (int i=0; i< iter; i++) {
            int newState = step(currentState);
            currentState = newState;
        }
        return currentState;
    }


    //private default constructor ==> can't be instantiated
    //side effect: class is final because it can't be subclassed:
    //super() can't be called from subclasses
    private SimpleModel() {
        throw new AssertionError();
    }

    public static double getThreshold() {
        return threshold;
    }
    public static void setThreshold(double threshold) {
        SimpleModel.threshold = threshold;
    }

    public static RandomGenerator getRandom() {
        return random;
    }

}
