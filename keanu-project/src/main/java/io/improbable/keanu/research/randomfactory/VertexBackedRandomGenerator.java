package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.special.Erf;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class VertexBackedRandomGenerator implements RandomGenerator {
    public List<GaussianVertex> randDoubleSource;    // used for all doubles
    public List<UniformVertex>     randIntSource;     // used for integers
    public List<Flip> randBoolSource;        // used for booleans
    public Random rand = new Random();

    private int gaussianCounter = 0;
    private int intCounter = 0;
    private int boolCounter = 0;

    public VertexBackedRandomGenerator(List<GaussianVertex> randDoubleSource, List<UniformVertex> randIntSource, List<Flip> randBoolSource) {
        this.randDoubleSource = randDoubleSource;
        this.randIntSource = randIntSource;
        this.randBoolSource = randBoolSource;
    }

    public VertexBackedRandomGenerator(int numberOfDoubles, int numberOfInts, int numberOfBools) {
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

//    @Override
//    public Double nextDouble(double min, double max) {
//        return min + (max - min) * nextDouble();
//    }
//
//    @Override
//    public Double nextDouble(Double min, Double max) {
//        return nextDouble(min.doubleValue(), max.doubleValue());
//    }
//
//    @Override
//    public Double nextDouble(double min, Double max) {
//        return nextDouble(min,max.doubleValue());
//    }
//
//    @Override
//    public Double nextDouble(Double min, double max) {
//        return nextDouble(min.doubleValue(),max);
//    }

    @Override
    public double nextDouble() {
        if(randDoubleSource == null) return rand.nextDouble();
        Double sample = (Erf.erf(randDoubleSource.get(gaussianCounter).getValue().scalar()) + 1.0)/2.0;
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.size();
        return sample;
    }

//    @Override
//    public Double nextGaussian(Double mu, Double sigma) {
//        return nextGaussian(mu.doubleValue(), sigma.doubleValue());
//    }
//
//    @Override
//    public Double nextGaussian(double mu, Double sigma) {
//        return nextGaussian(mu, sigma.doubleValue());
//    }
//
//    @Override
//    public Double nextGaussian(Double mu, double sigma) {
//        return nextGaussian(mu.doubleValue(), sigma);
//    }

    @Override
    public double nextGaussian() {
        if(randDoubleSource == null) return rand.nextGaussian();
        Double sample = randDoubleSource.get(gaussianCounter).getValue().scalar();
        gaussianCounter = (gaussianCounter+1)% randDoubleSource.size();
        return sample;
    }

    public VertexBackedRandomGenerator nextRandomFactory() {
        return new VertexBackedRandomGenerator(randDoubleSource, randIntSource, randBoolSource);
    }

    @Override
    public boolean nextBoolean() {
        if(randBoolSource == null) return rand.nextBoolean();
        Boolean sample = randBoolSource.get(boolCounter).getValue().scalar();
        boolCounter = (boolCounter+1)% randBoolSource.size();
        return sample;
    }

    @Override
    public float nextFloat() {
        return (float)nextDouble();
    }

    @Override
    public void setSeed(int i) {
        throw(new NotImplementedException());
    }

    @Override
    public void setSeed(int[] ints) {
        throw(new NotImplementedException());
    }

    @Override
    public void setSeed(long l) {
        throw(new NotImplementedException());
    }

    @Override
    public void nextBytes(byte[] bytes) {
        int i = 0;
        int j;
        int r;
        while(i<bytes.length) {
            r = nextInt();
            j = 0;
            while(j < 32 && i<bytes.length) {
                bytes[i++] = (byte)((r>>j)&0xff);
                j += 8;
            }
        }
    }

    @Override
    public int nextInt() {
        if(randIntSource == null) return rand.nextInt();
        Integer sample = (int)(1.0*Integer.MIN_VALUE + randIntSource.get(intCounter).getValue().scalar()*(1.0*Integer.MAX_VALUE - 1.0*Integer.MIN_VALUE + 1.0));
        intCounter = (intCounter+1)% randIntSource.size();
        return sample;
    }

    @Override
    public int nextInt(int i) {
        if(randIntSource == null) return rand.nextInt(i);
        Integer sample = (int)(randIntSource.get(intCounter).getValue().scalar()*i);
        intCounter = (intCounter+1)% randIntSource.size();
        return sample;
    }

    @Override
    public long nextLong() {
        return 0;
    }

}
