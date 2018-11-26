package KalmanFilter;

public class SimpleEnsembleModel {

    // No constructor necessary

    // Step function represents simple model function
    public static double[] step(double[] state) {
        double [] newState = new double[2]; // Initialise array of doubles
        newState[0] = 2*state[0] + 1; // newState[0] = x; therefore: (2x+1)
        newState[1] = 1.5*state[1] - .5; // "[1] = y; therefore: (1.5y-0.5)
        return newState;
    }

    // What we are observing from the model. In this case: x+y
    public static double observe(double[] state) {
        return state[0] + state[1];
    }
}
