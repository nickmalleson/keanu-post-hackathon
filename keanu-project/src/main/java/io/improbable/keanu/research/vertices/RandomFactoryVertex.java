package io.improbable.keanu.research.vertices;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

import java.util.ArrayList;
import java.util.List;

public class RandomFactoryVertex extends NonProbabilistic<VertexBackedRandomFactory> {

    public RandomFactoryVertex(List<GaussianVertex> randDoubleSource, List<UniformVertex> randIntSource, List<Flip> randBoolSource) {
        setValue(new VertexBackedRandomFactory(randDoubleSource, randIntSource, randBoolSource));
        addAllParents();
    }


    public RandomFactoryVertex(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        setValue(new VertexBackedRandomFactory(numberOfDoubles, numberOfInts, numberOfBools));
        addAllParents();
    }

    private void addAllParents() {
        if(getValue().randDoubleSource != null) addParents(getValue().randDoubleSource);
        if(getValue().randIntSource != null) addParents(getValue().randIntSource);
        if(getValue().randBoolSource != null) addParents(getValue().randBoolSource);
    }


    @Override
    public VertexBackedRandomFactory getDerivedValue() {
        return getValue();
    }

    @Override
    public VertexBackedRandomFactory sample(KeanuRandom random) {
        return getValue();
    }
}
