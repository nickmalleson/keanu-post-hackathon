package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.distributions.continuous.Gaussian;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class VertexBackedRandomFactory implements RandomFactory {
    public List<GaussianVertex> randDoubleSource;    // used for all doubles
    public List<UniformVertex>     randIntSource;     // used for integers
    public List<Flip> randBoolSource;        // used for booleans
    public Random rand = new Random();

    private int gaussianCounter = 0;
    private int intCounter = 0;
    private int boolCounter = 0;

    public VertexBackedRandomFactory(List<GaussianVertex> randDoubleSource, List<UniformVertex> randIntSource, List<Flip> randBoolSource) {
        this.randDoubleSource = randDoubleSource;
        this.randIntSource = randIntSource;
        this.randBoolSource = randBoolSource;
    }

    public VertexBackedRandomFactory(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        int i;
        if(numberOfDoubles > 0) {
            randDoubleSource = new ArrayList<>(numberOfDoubles);
            for (i=0; i<numberOfDoubles; ++i) {
                GaussianVertex v = new GaussianVertex(0.0, 1.0);
                v.sampleUsingDefaultRandom();
                randDoubleSource.add(v);
            }
        }
        if(numberOfInts > 0) {
            randIntSource = new ArrayList<>(numberOfInts);
            for (i=0; i<numberOfInts; ++i) {
                UniformVertex v = new UniformVertex(0.0, 1.0);
                v.sampleUsingDefaultRandom();
                randIntSource.add(v);
            }
        }
        if(numberOfBools > 0) {
            randBoolSource = new ArrayList<>(numberOfBools);
            for(i=0; i<numberOfBools; ++i) {
                Flip v = new Flip(0.5);
                v.sampleUsingDefaultRandom();
                randBoolSource.add(v);
            }
        }
    }

    public List<Vertex> getAllVertices() {
        List<Vertex> result = new ArrayList<>();
        if(randDoubleSource != null) result.addAll(randDoubleSource);
        if(randIntSource != null) result.addAll(randIntSource);
        if(randBoolSource != null) result.addAll(randBoolSource);
        return(result);
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
        if(randDoubleSource == null) return rand.nextDouble();
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
        if(randDoubleSource == null) return rand.nextGaussian();
        Double sample = randDoubleSource.get(gaussianCounter).getValue().scalar() * sigma + mu;
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.size();
        return sample;
    }

    public VertexBackedRandomFactory nextRandomFactory() {
        return new VertexBackedRandomFactory(randDoubleSource, randIntSource, randBoolSource);
    }

    @Override
    public Boolean nextBoolean() {
        if(randBoolSource == null) return rand.nextBoolean();
        Boolean sample = randBoolSource.get(boolCounter).getValue().scalar();
        boolCounter = (boolCounter+1)% randBoolSource.size();
        return sample;
    }

    @Override
    public Integer nextInt() {
        if(randIntSource == null) return rand.nextInt();
        Integer sample = (int)(1.0*Integer.MIN_VALUE + randIntSource.get(intCounter).getValue().scalar()*(1.0*Integer.MAX_VALUE - 1.0*Integer.MIN_VALUE + 1.0));
        intCounter = (intCounter+1)% randIntSource.size();
        return sample;
    }

    @Override
    public Integer nextInt(int i) {
        if(randIntSource == null) return rand.nextInt(i);
        Integer sample = (int)(randIntSource.get(intCounter).getValue().scalar()*i);
        intCounter = (intCounter+1)% randIntSource.size();
        return sample;
    }

    @Override
    public Integer nextInt(Integer i) {
        return nextInt(i.intValue());
    }
}
