package io.improbable.research

import io.improbable.keanu.algorithms.NetworkSamples
import org.apache.commons.math3.distribution.PoissonDistribution
import org.apache.commons.math3.random.MersenneTwister

class AbstractModel(var rhoS : Double, var rhoI : Double, var rhoR : Double) {
    val Nsamples = 10000 // number of samples of the concrete model
    val rand = MersenneTwister()
    var concreteStates = gamma()

    fun step() {
        concreteStates = gamma()
        concreteStates.forEach { model -> model.step() }
        setAbstractState()
    }

    fun setAbstractState() {
        rhoS = concreteStates.sumBy { model -> model.S }/Nsamples.toDouble()
        rhoI = concreteStates.sumBy { model -> model.I }/Nsamples.toDouble()
        rhoR = concreteStates.sumBy { model -> model.R }/Nsamples.toDouble()
    }

    fun gamma() : Array<SIRModel> {
        return Array<SIRModel>(Nsamples, {
            SIRModel(
                PoissonDistribution(rhoS).sample(),
                PoissonDistribution(rhoI).sample(),
                PoissonDistribution(rhoR).sample(),
                rand
            )
        })
    }

    fun dw_drhoS(m : SIRModel) : Double {
        return m.S/rhoS - 1.0
    }
    fun dw_drhoI(m : SIRModel) : Double {
        return m.I/rhoI - 1.0
    }
    fun dw_drhoR(m : SIRModel) : Double {
        return m.R/rhoR - 1.0
    }

    fun drhoS_dw(m : SIRModel) : Double {
        return m.S/Nsamples.toDouble()
    }
    fun drhoI_dw(m : SIRModel) : Double {
        return m.I/Nsamples.toDouble()
    }
    fun drhoR_dw(m : SIRModel) : Double {
        return m.R/Nsamples.toDouble()
    }

}