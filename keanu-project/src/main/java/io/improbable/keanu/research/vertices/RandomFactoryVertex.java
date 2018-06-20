package io.improbable.keanu.research.vertices;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomFactory;
import io.improbable.keanu.vertices.bool.probabilistic.Flip;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

public class RandomFactoryVertex extends NonProbabilistic<VertexBackedRandomFactory> {

    public RandomFactoryVertex(GaussianVertex randDoubleSource, UniformVertex randIntSource, Flip randBoolSource) {
        setValue(new VertexBackedRandomFactory(randDoubleSource, randIntSource, randBoolSource));
        addParent(randDoubleSource);
        addParent(randIntSource);
        addParent(randBoolSource);
    }

    public RandomFactoryVertex(int numberOfDoubles, int numberOfInts, int numberOfBools) {
        setValue(new VertexBackedRandomFactory(numberOfDoubles, numberOfInts, numberOfBools));
        addParent(getValue().randDoubleSource);
        addParent(getValue().randIntSource);
        addParent(getValue().randBoolSource);
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
