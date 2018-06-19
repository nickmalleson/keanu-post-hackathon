package io.improbable.keanu.research.randomfactory;

public interface IntRandomFactory<T> {
    T nextInt();
    T nextInt(int i);
    T nextInt(T i);
}
