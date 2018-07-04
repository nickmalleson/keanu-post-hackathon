package io.improbable.keanu.research.abstractinterpretation;

import io.improbable.keanu.kotlin.*;
import io.improbable.keanu.vertices.bool.BoolVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

public interface AbstractInterpretation<
    DOUBLETYPE extends DoubleOperators<DOUBLETYPE>,
    INTTYPE extends IntegerOperators<INTTYPE>,
    BOOLTYPE extends BooleanOperators<BOOLTYPE>
    > {
    DOUBLETYPE  newDouble(double x);
    INTTYPE     newInt(int i);
    BOOLTYPE    newBool(boolean b);
//    static public AbstractInterpretation<DoubleVertex, IntegerVertex, BoolVertex> vertexInterpretation = new AbstractInterpretation<>();
//    static public AbstractInterpretation<ArithmeticDouble, ArithmeticInteger, ArithmeticBoolean>  arithmeticInterpretation = new AbstractInterpretation<>();
}
