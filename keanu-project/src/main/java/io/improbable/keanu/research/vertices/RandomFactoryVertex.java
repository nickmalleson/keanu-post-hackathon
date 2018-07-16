package io.improbable.keanu.research.vertices;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

import java.util.List;

public class RandomFactoryVertex extends NonProbabilistic<VertexBackedRandomGenerator> {

    public RandomFactoryVertex(List<GaussianVertex> randDoubleSource, List<UniformVertex> randIntSource, List<Flip> randBoolSource) {
        setValue(new VertexBackedRandomGenerator(randDoubleSource, randIntSource, randBoolSource));
        addAllParents();
    }


    public RandomFactoryVertex(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        setValue(new VertexBackedRandomGenerator(numberOfDoubles, numberOfInts, numberOfBools));
        addAllParents();
    }

    private void addAllParents() {
        if(getValue().randDoubleSource != null) addParents(getValue().randDoubleSource);
        if(getValue().randIntSource != null) addParents(getValue().randIntSource);
        if(getValue().randBoolSource != null) addParents(getValue().randBoolSource);
    }


    @Override
    public VertexBackedRandomGenerator getDerivedValue() {
        return getValue();
    }

    @Override
    public VertexBackedRandomGenerator sample(KeanuRandom random) {
        return getValue();
    }
}
