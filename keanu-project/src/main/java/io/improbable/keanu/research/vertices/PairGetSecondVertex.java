package io.improbable.keanu.research.vertices;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda;
import org.apache.commons.math3.util.Pair;

public class PairGetSecondVertex<A, B> extends UnaryOpLambda<Pair<A, B>, B> {

    public PairGetSecondVertex(Vertex<Pair<A, B>> input) {
        super(input, Pair::getSecond);
    }
}
