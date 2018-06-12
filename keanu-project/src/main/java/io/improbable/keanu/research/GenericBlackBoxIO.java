package io.improbable.keanu.research;

import io.improbable.keanu.kotlin.BooleanOperators;
import io.improbable.keanu.kotlin.DoubleOperators;
import io.improbable.keanu.kotlin.IntegerOperators;
import io.improbable.keanu.tensor.Tensor;

public class GenericBlackBoxIO<DOUBLE, INTEGER , BOOL> {
    public Tensor<DOUBLE>   doubleValues; // TODO: Getters and setters? My arse.
    public Tensor<INTEGER>  integerValues;
    public Tensor<BOOL>     booleanValues;

    GenericBlackBoxIO() {
        this(null, null, null);
    }

    GenericBlackBoxIO(Tensor<DOUBLE> doubleValues) {
        this(doubleValues, null, null);
    }

    GenericBlackBoxIO(Tensor<DOUBLE> doubleValues, Tensor<INTEGER> integerValues) {
        this(doubleValues, integerValues, null);
    }

    GenericBlackBoxIO(Tensor<DOUBLE> doubleValues, Tensor<INTEGER> integerValues, Tensor<BOOL> booleanValues) {
        this.doubleValues = doubleValues;
        this.integerValues = integerValues;
        this.booleanValues = booleanValues;
    }


    // TODO: ...etc...

}
