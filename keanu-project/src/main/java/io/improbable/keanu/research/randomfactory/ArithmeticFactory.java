package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.kotlin.ArithmeticBoolean;
import io.improbable.keanu.kotlin.ArithmeticDouble;
import io.improbable.keanu.kotlin.ArithmeticInteger;

import java.util.Random;

public class ArithmeticFactory implements GenericRandomFactory<ArithmeticDouble, ArithmeticInteger, ArithmeticBoolean> {
    Random rand = new Random();

    @Override
    public ArithmeticDouble nextDouble(double min, double max) {
        return new ArithmeticDouble(rand.nextDouble()*(max-min)+min);
    }

    @Override
    public ArithmeticDouble nextDouble() {
        return new ArithmeticDouble(rand.nextDouble());
    }

    @Override
    public ArithmeticDouble nextConstant(double value) {
        return new ArithmeticDouble(value);
    }

    @Override
    public ArithmeticDouble nextGaussian(ArithmeticDouble mu, ArithmeticDouble sigma) {
        return nextGaussian(mu.getValue(), sigma.getValue());
    }

    @Override
    public ArithmeticDouble nextGaussian(double mu, ArithmeticDouble sigma) {
        return nextGaussian(mu, sigma.getValue());
    }

    @Override
    public ArithmeticDouble nextGaussian(ArithmeticDouble mu, double sigma) {
        return nextGaussian(mu.getValue(), sigma);
    }

    @Override
    public ArithmeticDouble nextGaussian(double mu, double sigma) {
        return new ArithmeticDouble(rand.nextGaussian()*sigma + mu);
    }

    @Override
    public ArithmeticBoolean nextBoolean() {
        return new ArithmeticBoolean(rand.nextBoolean());
    }

    @Override
    public ArithmeticInteger nextInt() {
        return null;
    }

    @Override
    public ArithmeticInteger nextInt(int i) {
        return null;
    }
}
