package io.improbable.keanu.research.abstractinterpretation

interface BoolExtensions<T,BOOL> {
    infix fun T.greaterThan(other : T) : BOOL
    // ...
}
