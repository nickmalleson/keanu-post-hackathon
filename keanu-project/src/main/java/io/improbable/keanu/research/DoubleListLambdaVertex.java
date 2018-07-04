package io.improbable.keanu.research;

import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.BiFunction;

public class DoubleListLambdaVertex extends UnaryOpLambda<DoubleTensor[], DoubleTensor[]> {

    public DoubleListLambdaVertex(Vertex<DoubleTensor[]> arrayOfInputs,
                                  BiFunction<DoubleTensor[], RandomGenerator, DoubleTensor[]> lambda,
                                  VertexBackedRandomGenerator random) {
        super(arrayOfInputs, (DoubleTensor[] in) -> lambda.apply(in, random));
    }
}
