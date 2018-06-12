package io.improbable.keanu.research;

import io.improbable.keanu.tensor.Tensor;

public class BlackBoxIO extends GenericBlackBoxIO<Double, Integer, Boolean> {

    BlackBoxIO() {
        super();
    }

    BlackBoxIO(Tensor<Double> doubleValues) {
        super(doubleValues);
    }

    BlackBoxIO(Tensor<Double> doubleValues, Tensor<Integer> integerValues) {
        super(doubleValues, integerValues);
    }

    BlackBoxIO(Tensor<Double> doubleValues, Tensor<Integer> integerValues, Tensor<Boolean> boolValues) {
        super(doubleValues, integerValues, boolValues);
    }

    // TODO: ...Yawn...
}

