package io.improbable.keanu.research

import io.improbable.keanu.network.BayesianNetwork
import io.improbable.keanu.research.visualisation.toGraphvizString
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import org.junit.Test

class ToGraphvizStringTest {
    @Test
    fun toGraphvizStringTest() {
        val a = GaussianVertex(0.0,1.0)
        val b = GaussianVertex(a,1.0)
        val c = GaussianVertex(a,1.0)
        val d = GaussianVertex(b,c)
        c.observe(1.0)
        val net = BayesianNetwork(setOf(a,b,c,d))
        println(net.toGraphvizString())
    }
}