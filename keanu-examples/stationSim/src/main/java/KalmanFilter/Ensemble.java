package KalmanFilter;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Arrays;

import static org.apache.commons.math3.optim.nonlinear.scalar.GoalType.*;

public class Ensemble {

    // Parameter
    public static int nSamples = 6;

    // RandomGenerator
    private static RandomGenerator rand = new MersenneTwister(2);

    // Variables
    double[][] particles;
    double[] weights;

    // Constructor
    // @params: MultivariateNormalDistribution g: i.e. Multivar Gauss
    //          Integer nSamples: no of samples
    public Ensemble(MultivariateNormalDistribution g, Integer nSamples) {

        // Initialise vars
        particles = new double[nSamples][2];
        weights = new double[nSamples];

        // loop through range:nSamples to populate arrays
        for (int i=0 ; i<nSamples; ++i) {
            weights[i] = 1.0/nSamples; // weights are uniform
            particles[i] = g.sample(); // populate particles[] with positions(x,y) (taken as sample from multivar norm dist)
        }
    }

    // forecast() method moves model forward one time step
    public void forecast() {

        // Loop through 1:particles.length
        for (int i=0; i<particles.length; ++i) {
            particles[i] = SimpleEnsembleModel.step(particles[i]); //step
        }
    }

    // analysis method
    // @params: observation: measured observation
    public void analysis(double observation, double noise) {
        
        for (int i=0; i<particles.length; ++i) {
            double newObs = SimpleEnsembleModel.observe(particles[i]); // Take observation
            double logLikelihood = Math.pow(observation - newObs, 2)/Math.pow(noise, 2); // (prevObs - newObs)^2 / noise^2
            double newWeights = logLikelihood + Math.log(weights[i]); // add logLikelihood to log of weights[]
            weights[i] = Math.exp(newWeights); // use exp to convert log weights to normal new weights
        }

        double counter = 0.0; // get total weights to use in next loop
        for (int i=0; i<particles.length; ++i) {
            counter += weights[i];
        }
        System.out.println("\nTotal value of weights: " + counter);

        System.out.println("\nPrinting new weights after normalising:");
        for (int i=0; i<particles.length; i++) {
            weights[i] = weights[i]/counter; // normalise weights using total of weights
            System.out.println(weights[i]);
        }
    }

    // Kullback-Leibler Divergence 'https://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence'
    // Formula found on wikipedia page. Top of two equations under *Definition* necessary as bottom one could result in divide_by_zero Error
    public double KLDivergence(MultivariateNormalDistribution norm) {

        double counter = 0.0;
        // This loop finds sum of Kullback-Leibler Divergence
        for (int i=0; i<particles.length; ++i) {
            double prob = -weights[i] * Math.log(norm.density(particles[i])/weights[i]);
            counter += prob;
        }
        return counter;
    } // ideally differentiate counter wrt norm

    // function to minimise KLDivergence (optimisation)
    public void minimiseKLDivergence(MultivariateNormalDistribution startState) {

        // function uses this BOBYQAOptimizer, don't know how it works
        // BOBY.. function optimises parameters, accepts list

        MultivariateOptimizer opt = new BOBYQAOptimizer(13);
        MultivariateFunction objective = weights -> { // lambda function

            // initialise means and covariances
            double[] means = new double[2];
            double[][] covariances = new double[2][2];

            // assign means and covariances one of weights
            means[0] = weights[0];
            means[1] = weights[1];
            covariances[0][0] = weights[2];
            covariances[0][1] = weights[3];
            covariances[1][0] = weights[4];
            covariances[1][1] = weights[5];

            // return new KLDivergence using new means and vars
            return KLDivergence(new MultivariateNormalDistribution(means, covariances));
        };

        //InitialGuess initialGuess = new InitialGuess(new double[] {123.456, 234.567} );
        double[] initialGuess = new double[] {123.456, 234.567};

        PointValuePair pointValuePair = opt.optimize(new MaxEval(100000),
                                                (OptimizationData) objective,
                                                new SimpleBounds(new double[]{-1000.0, -1000.0},
                                                                new double[]{1000.0, 1000.0}),
                                                MINIMIZE,
                                                new InitialGuess(initialGuess));

        System.out.println(pointValuePair.toString());
    }

    public static void getBestInterpolationVal() {

        // numberOfInterpolationPoints arg for BOBYQAOptimizer has to lie in specific range:
        //                    [(n+2), ((n+1)(n+2))/2]
        double[] interpolationRange = new double[2];
        interpolationRange[0] = nSamples + 2;
        interpolationRange[1] = ((nSamples + 1) * (nSamples + 2)) / 2;
        double meanInterpolationVal = (interpolationRange[1] + interpolationRange[0]) / 2;
        System.out.println("\nNumber of Interpolation points must lie in range " + interpolationRange[0] + " to " + interpolationRange[1]);
        System.out.println("Median point in range is: " + meanInterpolationVal);
        // Choices that exceed 2n+1 are not recommended
        double softMaximum = (2*nSamples) + 1;
        if (meanInterpolationVal > softMaximum) {
            System.out.println("Warning: Median point (" + meanInterpolationVal + ") is larger than recommended maximum (" + softMaximum + ")");
        }
    }

    public void printChecks() {
        System.out.println("\nStarting print checks...");
        System.out.println("\nPrinting weights:");
        System.out.println(Arrays.toString(weights));
        System.out.println("\nPrinting particles:");
        System.out.println(Arrays.deepToString(particles));
    }

    public static void main(String[] args) {

        // Method for checking what best interpolation value is for use with optimiser
        getBestInterpolationVal();

        // initialise means and covars for multivarnorm
        double[] mns = new double[2];
        double[][] covars = new double[2][2];

        /*
        * means = [5,3]
         */
        mns[0] = 5;
        mns[1] = 3;
        /*
        * covariances = [[32,15],
        *                [15,40]]
         */
        covars[0][0] = 32;
        covars[0][1] = 15;
        covars[1][0] = 15;
        covars[1][1] = 40;

        MultivariateNormalDistribution g = new MultivariateNormalDistribution(mns, covars);

        // Initialise ensemble
        System.out.println("\nInitialising Ensemble");
        Ensemble ensemble = new Ensemble(g, nSamples);

        ensemble.printChecks(); // Print checks

        // forecast (step) the particles
        ensemble.forecast();

        ensemble.printChecks(); // See how forecast() changes values

        // Produce initial observation
        double observation = 10.0;
        // run analysis
        ensemble.analysis(observation, rand.nextGaussian());
    }
}
