package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.UniformIntVertex;

public class VertexFactory implements RandomFactory<DoubleVertex, IntegerVertex, BoolVertex> {
    @Override
    public UniformVertex nextDouble(double min, double max) {
        UniformVertex uniformVertex = new UniformVertex(min, max);
//        uniformVertex.setValue(uniformVertex.sample(random));
        return uniformVertex;
    }

    @Override
    public DoubleVertex nextDouble() {
        return new UniformVertex(0.0,1.0);
    }

    @Override
    public ConstantDoubleVertex nextConstant(double value) {
        return new ConstantDoubleVertex(value);
    }

    @Override
    public GaussianVertex nextGaussian(double mu, double sigma) {
        GaussianVertex gaussianVertex = new GaussianVertex(mu, sigma);
//        gaussianVertex.setValue(gaussianVertex.sample(random));
        return gaussianVertex;
    }

    @Override
    public GaussianVertex nextGaussian(DoubleVertex mu, double sigma) {
        GaussianVertex gaussianVertex = new GaussianVertex(mu, sigma);
//        gaussianVertex.setValue(gaussianVertex.sample(random));
        return gaussianVertex;
    }

    @Override
    public GaussianVertex nextGaussian(DoubleVertex mu, DoubleVertex sigma) {
        GaussianVertex gaussianVertex = new GaussianVertex(mu, sigma);
//        gaussianVertex.setValue(gaussianVertex.sample(random));
        return gaussianVertex;
    }

    @Override
    public DoubleVertex nextGaussian(double mu, DoubleVertex sigma) {
        GaussianVertex gaussianVertex = new GaussianVertex(mu, sigma);
//        gaussianVertex.setValue(gaussianVertex.sample(random));
        return gaussianVertex;
    }

    @Override
    public BoolVertex nextBoolean() {
        return new Flip(0.5);
    }

    @Override
    public IntegerVertex nextInt() {
        return new UniformIntVertex(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public IntegerVertex nextInt(int i) {
        return new UniformIntVertex(0, i);
    }

}
