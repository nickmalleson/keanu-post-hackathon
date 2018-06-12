package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class NonDifferentiableBlackBox extends BlackBoxBase {

    public NonDifferentiableBlackBox(DoubleVertex doubleInputs, IntegerVertex integerInputs, BoolVertex boolInputs,
                                     BiFunction<BlackBoxIO, RandomFactory<Double>, BlackBoxIO> model) {
        super(doubleInputs, integerInputs, boolInputs);
        // TODO: create the output vertices from the model and the inputs
    }

}
