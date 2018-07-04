package io.improbable.keanu.research.abstractinterpretation;

import io.improbable.keanu.kotlin.ArithmeticBoolean;
import io.improbable.keanu.kotlin.ArithmeticDouble;
import io.improbable.keanu.kotlin.ArithmeticInteger;

public class ArithmeticInterpretation implements AbstractInterpretation<ArithmeticDouble,ArithmeticInteger,ArithmeticBoolean> {
    @Override
    public ArithmeticDouble newDouble(double x) {
        return new ArithmeticDouble(x);
    }

    @Override
    public ArithmeticInteger newInt(int i) {
        return new ArithmeticInteger(i);
    }

    @Override
    public ArithmeticBoolean newBool(boolean b) {
        return new ArithmeticBoolean(b);
    }
}
