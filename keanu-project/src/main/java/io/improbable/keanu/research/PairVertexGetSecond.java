package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.util.Pair;

public class PairVertexGetSecond<A, B> extends UnaryOpLambda<Pair<A, B>, B> {

    public PairVertexGetSecond(Vertex<Pair<A, B>> input) {
        super(input, Pair::getSecond);
    }
}
