package io.improbable.keanu.research.vertices;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.dbl.Nd4jDoubleTensor;
import io.improbable.keanu.vertices.Vertex;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class TensorJoinVertex extends ReduceVertex<Tensor<Double>, Tensor<Double>> {
    TensorJoinVertex(Collection<? extends Vertex<Tensor<Double>>> inputs) {
        super(inputs, (List<Tensor<Double>> ins) -> {
            Tensor<Double> joined = new Nd4jDoubleTensor(new double[ins.size()], new int []{ins.size(), 1});
            for(int i=0; i<ins.size(); ++i) {
                joined.setValue(ins.get(i).getValue(0),i);
            }
            return joined;
        });
    }
}
