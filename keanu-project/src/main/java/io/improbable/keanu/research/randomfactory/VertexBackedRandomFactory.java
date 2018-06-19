package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;

public class VertexBackedRandomFactory implements RandomFactory {
    public ArrayList<GaussianVertex>    randDoubleSource;    // used for all doubles
    public ArrayList<UniformVertex>     randIntSource;     // used for integers
    public ArrayList<Flip>              randBoolSource;        // used for booleans

    private int gaussianCounter = 0;
    private int intCounter = 0;
    private int boolCounter = 0;

    public VertexBackedRandomFactory(ArrayList<GaussianVertex> randDoubleSource, ArrayList<UniformVertex> randIntSource, ArrayList<Flip> randBoolSource) {
        this.randDoubleSource = randDoubleSource;
        this.randIntSource = randIntSource;
        this.randBoolSource = randBoolSource;
    }

    public VertexBackedRandomFactory(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        int i;
        randDoubleSource = new ArrayList<>(numberOfDoubles);
        randIntSource = new ArrayList<>(numberOfInts);
        randBoolSource = new ArrayList<>(numberOfBools);
        for (i=0; i<numberOfDoubles; ++i) {
            randDoubleSource.add(new GaussianVertex(0.0, 1.0));
        }
        for (i=0; i<numberOfInts; ++i) {
            randIntSource.add(new UniformVertex(0.0,1.0));
        }
        for(i=0; i<numberOfBools; ++i) {
            randBoolSource.add(new Flip(0.5));
        }
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
        Double sample = (Erf.erf(randDoubleSource.get(gaussianCounter).getValue().scalar()) + 1.0)/2.0;
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.size();
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
        Double sample = randDoubleSource.get(gaussianCounter).getValue().scalar() * sigma + mu;
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.size();
        return sample;
    }

    public VertexBackedRandomFactory nextRandomFactory() {
        return new VertexBackedRandomFactory(randDoubleSource, randIntSource, randBoolSource);
    }

    @Override
    public Boolean nextBoolean() {
        Boolean sample = randBoolSource.get(boolCounter).getValue().scalar();
        boolCounter = (boolCounter+1)% randBoolSource.size();
        return sample;
    }

    @Override
    public Integer nextInt() {
        Integer sample = (int)(1.0*Integer.MIN_VALUE + randIntSource.get(intCounter).getValue().scalar()*(1.0*Integer.MAX_VALUE - 1.0*Integer.MIN_VALUE + 1.0));
        intCounter = (intCounter+1)% randIntSource.size();
        return sample;
    }

    @Override
    public Integer nextInt(int i) {
        Integer sample = (int)(randIntSource.get(intCounter).getValue().scalar()*i);
        intCounter = (intCounter+1)% randIntSource.size();
        return sample;
    }

    @Override
    public Integer nextInt(Integer i) {
        return nextInt(i.intValue());
    }
}
