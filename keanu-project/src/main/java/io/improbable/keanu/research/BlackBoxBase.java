package io.improbable.keanu.research;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.generic.GenericTensor;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

import java.util.function.Function;

public class BlackBoxBase {
    public DoubleVertex     doubleInputs;
    public IntegerVertex    integerInputs;
    public BoolVertex       booleanInputs;
    public DoubleVertex     doubleOutputs;
    public IntegerVertex    integerOutputs;
    public BoolVertex       booleanOutputs;

    BlackBoxBase(DoubleVertex doubleInputs, IntegerVertex integerInputs, BoolVertex boolInputs) {
        this.doubleInputs = doubleInputs;
        this.integerInputs = integerInputs;
        this.booleanInputs = boolInputs;
    }



    <TIN, TOUT> GenericTensor<TOUT> tensorMap(Tensor<TIN> T, Function<TIN,TOUT> mapFunction) {
        return new GenericTensor<>(
            T.asFlatList().stream().map(mapFunction).toArray((int i) -> (TOUT [])new Object[i]), T.getShape()
        );
    }

}
