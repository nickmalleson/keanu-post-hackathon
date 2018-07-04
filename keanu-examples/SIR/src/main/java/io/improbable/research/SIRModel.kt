package io.improbable.research

import io.improbable.keanu.kotlin.BooleanOperators
import io.improbable.keanu.kotlin.DoubleOperators
import io.improbable.keanu.kotlin.IntegerOperators
import io.improbable.keanu.research.randomfactory.GenericRandomFactory
import org.apache.commons.math3.distribution.BinomialDistribution

class SIRModel<DOUBLE : DoubleOperators<DOUBLE>, INT : IntegerOperators<INT>, BOOL : BooleanOperators<BOOL>>(
    var S : INT, var I : INT, var R : INT, val rand : GenericRandomFactory<DOUBLE, INT, BOOL>) {
    val beta = 0.1  // contact rate
    val gamma = 0.1 // recovery rate


    fun step() {
    }
}