package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.operators.binary.DoubleBinaryOpLambda;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

import java.util.ArrayList;

public class DoubleArrayIndexingVertex extends DoubleBinaryOpLambda<Double[], Integer> {

    public DoubleArrayIndexingVertex(Vertex<Double[]> input, Vertex<Integer> index) {
        super(input, index, (Double[] in, Integer i) -> in[i]);
    }

    public DoubleArrayIndexingVertex(Vertex<Double[]> input, Integer index) {
        this(input, new ConstantIntegerVertex(index));
    }

//    public DoubleArrayIndexingVertex(Vertex<ArrayList<Double>> input, Integer index) {
//        super(input, new ConstantIntegerVertex(index), (ArrayList<Double> in, Integer i) -> in.get(i));
//    }
}
