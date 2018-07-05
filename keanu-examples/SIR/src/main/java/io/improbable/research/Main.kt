package io.improbable.research

import org.apache.commons.math3.random.MersenneTwister
import java.io.FileWriter

fun main(args : Array<String>) {
    val file = FileWriter("data.out")
    val model = AbstractModel(96.0, 4.0, 0.01)
    for(step in 1..40) {
        model.step()
        val out = "$step ${model.rhoS} ${model.rhoI} ${model.rhoR}\n"
        file.write(out)
        print(out)
    }

    file.close()
}

fun runConcrete() {
    val file = FileWriter("data.out")
    val model = SIRModel(96, 4, 0, MersenneTwister())
    for(step in 1..40) {
        model.step()
        file.write("$step ${model.S} ${model.I} ${model.R}\n")
        println("$step ${model.S} ${model.I} ${model.R}")
    }
    file.close()
}