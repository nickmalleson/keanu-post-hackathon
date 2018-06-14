package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import org.apache.commons.math3.special.Erf;

import java.util.ArrayList;

public class VertexBackedRandomFactory implements RandomFactory {
    public ArrayList<GaussianVertex>    listOfGaussians;
    public ArrayList<UniformVertex>     listOfUniforms;
    public ArrayList<Flip>              listOfFlips;

    private int gaussianCounter = 0;
    private int intCounter = 0;
    private int boolCounter = 0;

    public VertexBackedRandomFactory(ArrayList<GaussianVertex> listOfGaussians, ArrayList<UniformVertex> listOfUniforms, ArrayList<Flip> listOfFlips) {
        this.listOfGaussians = listOfGaussians;
        this.listOfUniforms = listOfUniforms;
        this.listOfFlips = listOfFlips;
    }

    public VertexBackedRandomFactory(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        int i;
        listOfGaussians = new ArrayList<>(numberOfDoubles);
        for (i=0; i<numberOfDoubles; ++i) {
            listOfGaussians.add(new GaussianVertex(0.0, 1.0));
        }
        for (i=0; i<numberOfInts; ++i) {
            listOfUniforms.add(new UniformVertex(0.0,1.0));
        }
        for(i=0; i<numberOfBools; ++i) {
            listOfFlips.add(new Flip(0.5));
        }
    }

    @Override
    public Double nextDouble(double min, double max) {
        return min + (max - min) * nextDouble();
    }

    @Override
    public Double nextDouble() {
        Double sample = (Erf.erf(listOfGaussians.get(gaussianCounter).getValue().scalar()) + 1.0)/2.0;
        gaussianCounter = (gaussianCounter+1)%listOfGaussians.size();
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
        Double sample = listOfGaussians.get(gaussianCounter).getValue().scalar() * sigma + mu;
        gaussianCounter = (gaussianCounter+1)%listOfGaussians.size();
        return sample;
    }

    public VertexBackedRandomFactory nextRandomFactory() {
        return new VertexBackedRandomFactory(listOfGaussians, listOfUniforms, listOfFlips);
    }

    @Override
    public Boolean nextBoolean() {
        Boolean sample = listOfFlips.get(boolCounter).getValue().scalar();
        boolCounter = (boolCounter+1)%listOfFlips.size();
        return sample;
    }

    @Override
    public Integer nextInt() {
        Integer sample = (int)(1.0*Integer.MIN_VALUE + listOfUniforms.get(intCounter).getValue().scalar()*(1.0*Integer.MAX_VALUE - 1.0*Integer.MIN_VALUE + 1.0));
        intCounter = (intCounter+1)% listOfUniforms.size();
        return sample;
    }

    @Override
    public Integer nextInt(int i) {
        Integer sample = (int)(listOfUniforms.get(intCounter).getValue().scalar()*i);
        intCounter = (intCounter+1)% listOfUniforms.size();
        return sample;
    }
}
