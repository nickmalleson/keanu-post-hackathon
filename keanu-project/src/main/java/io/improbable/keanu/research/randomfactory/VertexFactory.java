package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.research.abstractinterpretation.VertexInterpretation;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.UniformIntVertex;

import java.util.HashSet;
import java.util.Set;

public class VertexFactory extends VertexInterpretation implements GenericRandomFactory<DoubleVertex, IntegerVertex, BoolVertex> {
    Set<Vertex> allCreatedVertices = new HashSet<>();


    public Set<Vertex> getAllCreatedVertices() {
        return allCreatedVertices;
    }

    @Override
    public UniformVertex nextDouble(double min, double max) {
        return nextDouble(new ConstantDoubleVertex(min), new ConstantDoubleVertex(max));
    }

    @Override
    public UniformVertex nextDouble(DoubleVertex min, double max) {
        return nextDouble(min, new ConstantDoubleVertex(max));
    }

    @Override
    public UniformVertex nextDouble(double min, DoubleVertex max) {
        return nextDouble(new ConstantDoubleVertex(min), max);
    }

    @Override
    public UniformVertex nextDouble(DoubleVertex min, DoubleVertex max) {
        return nextUniformVertex(min, max);
    }

    @Override
    public UniformVertex nextDouble() {
        return nextDouble(0.0,1.0);
    }

    @Override
    public GaussianVertex nextGaussian(double mu, double sigma) {
        return nextGaussianVertex(new ConstantDoubleVertex(mu), new ConstantDoubleVertex(sigma));
    }

    @Override
    public GaussianVertex nextGaussian(DoubleVertex mu, double sigma) {
        return nextGaussianVertex(mu, new ConstantDoubleVertex(sigma));
    }

    @Override
    public GaussianVertex nextGaussian(DoubleVertex mu, DoubleVertex sigma) {
        return nextGaussianVertex(mu, sigma);
    }

    @Override
    public GaussianVertex nextGaussian(double mu, DoubleVertex sigma) {
        return nextGaussianVertex(new ConstantDoubleVertex(mu), sigma);
    }

    @Override
    public Flip nextBoolean() {
        return nextFlipVertex(new ConstantDoubleVertex(0.5));
    }

    @Override
    public UniformIntVertex nextInt() {
        // TODO: This will never return Integer.MAX_VALUE
        // TODO: which is a bug
        return nextUniformIntVertex(
            new ConstantIntegerVertex(Integer.MIN_VALUE),
            new ConstantIntegerVertex(Integer.MAX_VALUE)
        );
    }

    @Override
    public UniformIntVertex nextInt(int i) {
        return nextInt(new ConstantIntegerVertex(i));
    }

    @Override
    public UniformIntVertex nextInt(IntegerVertex i) {
        return nextUniformIntVertex(new ConstantIntegerVertex(0), i);
    }


    protected UniformIntVertex nextUniformIntVertex(IntegerVertex min, IntegerVertex max) {
        UniformIntVertex vertex = new UniformIntVertex(min, max);
        allCreatedVertices.add(vertex);
        return vertex;
    }

    protected UniformVertex nextUniformVertex(DoubleVertex min, DoubleVertex max) {
        UniformVertex vertex = new UniformVertex(min, max);
        allCreatedVertices.add(vertex);
        return vertex;
    }

    protected GaussianVertex nextGaussianVertex(DoubleVertex mu, DoubleVertex sigma) {
        GaussianVertex vertex = new GaussianVertex(mu, sigma);
        allCreatedVertices.add(vertex);
        return vertex;
    }

    protected Flip nextFlipVertex(DoubleVertex p) {
        Flip vertex = new Flip(p);
        allCreatedVertices.add(vertex);
        return vertex;
    }

}
