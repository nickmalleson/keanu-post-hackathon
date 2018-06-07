package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;

import java.util.ArrayList;

public class VertexBackedRandomFactory implements RandomFactory<Double> {

    public ArrayList<GaussianVertex> listOfGaussians;
    public ArrayList<UniformVertex> listOfUniforms;
    private int counterForGaussians = 0;
    private int counterForUniform = 0;

    public VertexBackedRandomFactory(ArrayList<GaussianVertex> listOfGaussians, ArrayList<UniformVertex> listOfUniforms) {
        this.listOfGaussians = listOfGaussians;
        this.listOfUniforms = listOfUniforms;
    }

    public VertexBackedRandomFactory(int numberOfGaussians, int numberOfUniforms) {
        listOfGaussians = new ArrayList<>(numberOfGaussians);
        listOfUniforms = new ArrayList<>(numberOfUniforms);
        for (int i=0; i<numberOfGaussians; i++) {
            listOfGaussians.add(new GaussianVertex(0.0, 1.0));
        }
        for (int i=0; i<numberOfUniforms; i++) {
            listOfUniforms.add(new UniformVertex(0.0, 1.0));
        }
    }

    @Override
    public void setRandom(KeanuRandom random) {}

    @Override
    public Double nextDouble(double min, double max) {
        return min + (max - min) * listOfUniforms.get(counterForUniform++%listOfUniforms.size()).getValue().scalar();
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
        return listOfGaussians.get(counterForGaussians++%listOfGaussians.size()).getValue().scalar() * sigma + mu;
    }

    public VertexBackedRandomFactory nextRandomFactory() {
        return new VertexBackedRandomFactory(listOfGaussians, listOfUniforms);
    }
}
