package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.apache.commons.math3.special.Erf;


public class VertexBackedRandomFactory implements RandomFactory {
    public GaussianVertex       randDoubleSource;    // used for all doubles
    public UniformVertex        randIntSource;     // used for integers
    public Flip                 randBoolSource;        // used for booleans

    public int gaussianCounter = 0;
    private int intCounter = 0;
    private int boolCounter = 0;

    public VertexBackedRandomFactory(GaussianVertex randDoubleSource, UniformVertex randIntSource, Flip randBoolSource) {
        this.randDoubleSource = randDoubleSource;
        this.randIntSource = randIntSource;
        this.randBoolSource = randBoolSource;
    }

    public VertexBackedRandomFactory(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        randDoubleSource = new GaussianVertex(new int []{numberOfDoubles}, 0.0, 1.0);
        randDoubleSource.sampleUsingDefaultRandom();
        //System.out.println(randDoubleSource.getShape()[0]);
        randIntSource = new UniformVertex(new int[]{numberOfInts}, 0.0, 1.0);
        randBoolSource = new Flip(new int[]{numberOfBools}, 0.5);
    }

    @Override
    public Double nextDouble(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    @Override
    public Double nextDouble(Double min, Double max) {
        return nextDouble(min.doubleValue(), max.doubleValue());
    }

    @Override
    public Double nextDouble(double min, Double max) {
        return nextDouble(min,max.doubleValue());
    }

    @Override
    public Double nextDouble(Double min, double max) {
        return nextDouble(min.doubleValue(),max);
    }

    @Override
    public Double nextDouble() {
        Double sample = (Erf.erf(randDoubleSource.getValue().getValue(gaussianCounter)) + 1.0)/2.0;
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.getShape()[0];
        //System.out.println(gaussianCounter);
        //System.out.println("hi");
        //System.out.println(randDoubleSource.getShape()[0]);
        return sample;
    }

    @Override
    public Double nextConstant(double value) {
        return value;
    }

    @Override
    public Double nextGaussian(Double mu, Double sigma) {
        return nextGaussian(mu.doubleValue(), sigma.doubleValue());
    }

    @Override
    public Double nextGaussian(double mu, Double sigma) {
        return nextGaussian(mu, sigma.doubleValue());
    }

    @Override
    public Double nextGaussian(Double mu, double sigma) {
        return nextGaussian(mu.doubleValue(), sigma);
    }

    @Override
    public Double nextGaussian(double mu, double sigma) {
        Double sample = randDoubleSource.getValue().getValue(gaussianCounter) * sigma + mu;
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.getShape()[0];
        return sample;
    }

    public VertexBackedRandomFactory nextRandomFactory() {
        return new VertexBackedRandomFactory(randDoubleSource, randIntSource, randBoolSource);
    }

    @Override
    public Boolean nextBoolean() {
        Boolean sample = randBoolSource.getValue().getValue(boolCounter);
        boolCounter = (boolCounter+1)% randBoolSource.getShape()[0];
        return sample;
    }

    @Override
    public Integer nextInt() {
        Integer sample = (int)(1.0*Integer.MIN_VALUE + randIntSource.getValue().getValue(intCounter)*(1.0*Integer.MAX_VALUE - 1.0*Integer.MIN_VALUE + 1.0));
        intCounter = (intCounter+1)% randIntSource.getShape()[0];
        return sample;
    }

    @Override
    public Integer nextInt(int i) {
        Integer sample = (int)(randIntSource.getValue().getValue(intCounter)*i);
        intCounter = (intCounter+1)% randIntSource.getShape()[0];
        return sample;
    }

    @Override
    public Integer nextInt(Integer i) {
        return nextInt(i.intValue());
    }
}
