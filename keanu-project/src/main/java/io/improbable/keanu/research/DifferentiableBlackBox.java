package io.improbable.keanu.research;

import io.improbable.keanu.kotlin.ArithmeticBoolean;
import io.improbable.keanu.kotlin.ArithmeticInteger;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.diff.DualNumber;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

import java.util.function.BiFunction;

public class DifferentiableBlackBox extends BlackBoxBase {


    public DifferentiableBlackBox(DoubleVertex doubleInputs, IntegerVertex integerInputs, BoolVertex boolInputs,
                                     BiFunction<
                                         AbstractBlackBoxIO<DualNumber, ArithmeticInteger, ArithmeticBoolean>,
                                         RandomFactory<Double>,
                                         AbstractBlackBoxIO<DualNumber, ArithmeticInteger, ArithmeticBoolean>
                                         > model) {
        super(doubleInputs, integerInputs, boolInputs);
        // TODO: create the output vertices from the model and the inputs
    }




}
