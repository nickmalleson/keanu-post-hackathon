package io.improbable.keanu.research.randomfactory;

import io.improbable.keanu.kotlin.BooleanOperators;
import io.improbable.keanu.kotlin.DoubleOperators;
import io.improbable.keanu.kotlin.IntegerOperators;
import io.improbable.keanu.research.abstractinterpretation.AbstractInterpretation;

public interface GenericRandomFactory<
    DOUBLETYPE extends DoubleOperators<DOUBLETYPE>,
    INTTYPE extends IntegerOperators<INTTYPE>,
    BOOLTYPE extends BooleanOperators<BOOLTYPE>
    > extends DoubleRandomFactory<DOUBLETYPE>, IntRandomFactory<INTTYPE>, BoolRandomFactory<BOOLTYPE>
{

}
