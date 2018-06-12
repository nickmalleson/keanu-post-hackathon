package io.improbable.keanu.research;

import io.improbable.keanu.kotlin.BooleanOperators;
import io.improbable.keanu.kotlin.DoubleOperators;
import io.improbable.keanu.kotlin.IntegerOperators;
import io.improbable.keanu.tensor.Tensor;

public class AbstractBlackBoxIO<DOUBLE extends DoubleOperators<DOUBLE>, INTEGER  extends IntegerOperators<INTEGER>, BOOL extends BooleanOperators<BOOL>>  extends GenericBlackBoxIO<DOUBLE, INTEGER, BOOL> {

    AbstractBlackBoxIO() {
        super();
    }

    AbstractBlackBoxIO(Tensor<DOUBLE> doubleValues) {
        super(doubleValues);
    }

    AbstractBlackBoxIO(Tensor<DOUBLE> doubleValues, Tensor<INTEGER> integerValues) {
        super(doubleValues, integerValues);
    }

    AbstractBlackBoxIO(Tensor<DOUBLE> doubleValues, Tensor<INTEGER> integerValues, Tensor<BOOL> boolValues) {
        super(doubleValues, integerValues, boolValues);
    }


    // TODO: ...etc...


}
