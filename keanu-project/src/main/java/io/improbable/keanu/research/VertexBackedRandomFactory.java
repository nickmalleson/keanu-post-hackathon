package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;

import java.util.Random;

public class VertexBackedRandomFactory implements RandomFactory<Double> {

    public GaussianVertex[] listOfGaussians;
    public UniformVertex[] listOfUniforms;
    private int counterForGaussians = 0;
    private int counterForUniform = 0;

    public VertexBackedRandomFactory(GaussianVertex[] listOfGaussains, UniformVertex[] listOfUniforms) {
        this.listOfGaussians = listOfGaussains;
        this.listOfUniforms = listOfUniforms;
    }

    public VertexBackedRandomFactory(int numberOfGaussians, int numberOfUniforms) {
        listOfGaussians = new GaussianVertex[numberOfGaussians];
        listOfUniforms = new UniformVertex[numberOfUniforms];
        for (int i=0; i<numberOfGaussians; i++) {
            listOfGaussians[i] = new GaussianVertex(0.0, 1.0);
        }
        for (int i=0; i<numberOfUniforms; i++) {
            listOfUniforms[i] = new UniformVertex(0.0, 1.0);
        }
    }

    @Override
    public void setRandom(Random random) {}

    @Override
    public Double nextDouble(double min, double max) {
        return min + (max - min) * listOfUniforms[counterForUniform++].getValue();
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
        return listOfGaussians[counterForGaussians++].getValue() * sigma + mu;
    }
}
