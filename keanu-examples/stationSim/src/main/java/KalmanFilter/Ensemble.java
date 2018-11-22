package KalmanFilter;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

public class Ensemble {

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
            weights[i] = 1.0/nSamples; // weights are (uniform?) (equivalent?)
            particles[i] = g.sample(); // populate particles[] with positions (taken as sample from multivar norm dist
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
    // @params: observation: measured observation? previous observation?
    public void analysis(double observation, double noise) {

        // Would observation need to be an array of observations? or is observation a constant?
        
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

        for (int i=0; i<particles.length; i++) {
            weights[i] = weights[i]/counter; // normalise weights using total of weights
            System.out.println(weights[i]);
        }
    }

    // Kullback-Leibler Divergence 'https://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence'
    // Formula found on wikipedia page. Top of two equations under *Definition* necessary as bottom one could result in divide_by_zero Error
    public double KLDivergence(MultivariateNormalDistribution norm) {

        double counter = 0.0;
        for (int i=0; i<particles.length; ++i) {
            double prob = -weights[i] * Math.log(norm.density(particles[i])/weights[i]);
            counter += prob;
        }
        return counter;
    } // ideally differentiate counter wrt norm but we were lazy(?)

    // function to minimise KLDivergence (optimisation?)
    public void minimiseKLDivergence(MultivariateNormalDistribution startState) {

        // function uses this BOBYQAOptimizer, don't know how it works
        // BOBY.. function optimises parameters, but doesn't know what params it has to optimise
        // accepts list of params, we're organising into means and covars?
        MultivariateOptimizer opt = new BOBYQAOptimizer(15);
        MultivariateFunction objective = weights -> { // lambda function

            // initialise means and covariances
            double[] means = new double[2];
            double[][] covars = new double[2][2];

            // assign means and covariances one of weights
            means[0] = weights[0];
            means[1] = weights[1];
            covars[0][0] = weights[2];
            covars[0][1] = weights[3];
            covars[1][0] = weights[4];
            covars[1][1] = weights[5];

            // return new KLDivergence using new means and vars
            return KLDivergence(new MultivariateNormalDistribution(means, covars));
        };
    }
}
