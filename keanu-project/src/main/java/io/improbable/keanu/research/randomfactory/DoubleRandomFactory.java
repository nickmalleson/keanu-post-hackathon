package io.improbable.keanu.research.randomfactory;

public interface DoubleRandomFactory<T> {
    T nextDouble(double min, double max);

    T nextDouble(T min, T max);

    T nextDouble(double min, T max);

    T nextDouble(T min, double max);

    T nextDouble();

    T nextConstant(double value);

    T nextGaussian(T mu, T sigma);

    T nextGaussian(double mu, T sigma);

    T nextGaussian(T mu, double sigma);

    T nextGaussian(double mu, double sigma);

    default T nextGaussian() {
        return nextGaussian(0.0, 1.0);
    }

}
