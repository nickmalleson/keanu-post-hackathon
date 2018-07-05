package io.improbable.research

import org.apache.commons.math3.distribution.BinomialDistribution
import org.apache.commons.math3.random.RandomGenerator

class SIRModel(var S : Int, var I : Int, var R : Int, val rand : RandomGenerator) {
    val beta = 0.8  // contact rate
    val gamma = 0.2 // recovery rate


    fun step() {
        val newInfections = BinomialDistribution(rand,(S*I.toDouble()/N()).toInt(), beta).sample()
        val recoveries = BinomialDistribution(rand, I, gamma).sample()
        S -= newInfections
        I += newInfections - recoveries
        R += recoveries
//        println("stepped model $S $I $R")
    }

    fun N() : Int {
        return S+I+R
    }
}