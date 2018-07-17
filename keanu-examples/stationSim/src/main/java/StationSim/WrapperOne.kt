package StationSim


import io.improbable.keanu.algorithms.NetworkSamples
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings
import io.improbable.keanu.network.BayesianNetwork
import io.improbable.keanu.research.randomfactory.VertexBackedRandomGenerator
import io.improbable.keanu.research.visualisation.*
import io.improbable.keanu.research.vertices.IntegerArrayIndexingVertex
import io.improbable.keanu.research.vertices.RandomFactoryVertex
import io.improbable.keanu.vertices.dbl.nonprobabilistic.CastDoubleVertex
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import io.improbable.keanu.vertices.generic.nonprobabilistic.operators.unary.UnaryOpLambda
import org.apache.commons.math3.random.RandomGenerator

import java.io.*
import java.util.Arrays
import java.util.function.Function


/**
 * Created by nick on 22/06/2018.
 */
//    static ArrayList<List<IntegerTensor>> results = new ArrayList<List<IntegerTensor>>();

class Wrapper {
    companion object {

        internal var stationSim = Station(System.currentTimeMillis())
        private val numTimeSteps = 1000
        var numRandomDoubles = 10
        private val numSamples = 7000
        private val dropSamples = 1000
        private val downSample = 3
        //private static boolean OBSERVE = true;
        private val sigmaNoise = 0.1 // The amount of noise to be added to the truth

        fun writeResults(samples: List<Array<Int?>>, truth: Array<Int?>, observed: Boolean?, obInterval: Int) {
            var writer: Writer? = null
            val tempStation = Station(System.currentTimeMillis())
            val totalNumPeople = tempStation.numPeople

            val dirName = "results/"
            val params = "OBSERVE" + observed + "obInterval" + obInterval + "_numSamples" + numSamples + "_numTimeSteps" + numTimeSteps + "_numRandomDoubles" + numRandomDoubles + "_totalNumPeople" + totalNumPeople + "_dropSamples" + dropSamples + "_downSample" + "_sigmaNoise" + sigmaNoise + "_downsample" + downSample + "_timeStamp" + System.currentTimeMillis()

            // Write out samples
            try {
                writer = BufferedWriter(OutputStreamWriter(
                    FileOutputStream(dirName + "Samples_" + params + ".csv"),
                    "utf-8"))
                for (i in samples.indices) {
                    val peoplePerIter = samples[i]
                    for (j in peoplePerIter.indices) {
                        writer.write(peoplePerIter[j].toString() + "")
                        if (j != peoplePerIter.size - 1) {
                            writer.write(",")
                        }
                    }
                    writer.write(System.lineSeparator())
                }
            } catch (ex: IOException) {
                println("Error writing to file")
            } finally {
                try {
                    writer!!.close()
                } catch (ex: Exception) {
                    println("Error closing file")
                }

            }

            // Write out Truth
            try {
                writer = BufferedWriter(OutputStreamWriter(
                    FileOutputStream(dirName + "Truth_" + params + ".csv"),
                    "utf-8"))
                for (i in truth.indices) {
                    writer.write(truth[i].toString() + "")
                    if (i != truth.size - 1) {
                        writer.write(",")
                    }
                }
                writer.write(System.lineSeparator())

            } catch (ex: IOException) {
                println("Error writing to file")
            } finally {
                try {
                    writer!!.close()
                } catch (ex: Exception) {
                    println("Error closing file")
                }

            }
        }


        fun run(rand: RandomGenerator): Array<Int?> {
            println("Model " + Station.modelCount++ + " starting")
            stationSim.start(rand)

            val numPeople = arrayOfNulls<Int>(numTimeSteps)
            for (i in 0 until numTimeSteps) {
                numPeople[i] = 0
            }

            var i = 0
            do {
                // Run a step of each simulation
                if (!stationSim.schedule.step(stationSim)) {
                    break
                }
                numPeople[i] = stationSim.area.getAllObjects().size
                i++
            } while (stationSim.area.getAllObjects().size > 0 && i < numTimeSteps)
            stationSim.finish()

            //      results.add(Arrays.asList(numPeople));

            return numPeople
        }

        fun keanu(truth: Array<Int?>, observe: Boolean, obInterval: Int): List<Array<Int?>> {

            println("Initialising random number stream")

            //VertexBackedRandomFactory random = new VertexBackedRandomFactory(numInputs,, 0, 0);
            val random = RandomFactoryVertex(numRandomDoubles, 0, 0)


            // This is the 'black box' vertex that runs the model. It's input is the random numbers and
            // output is a list of Integer(tensor)s (the number of agents in the model at each iteration).
            //BlackBox box = new BlackBox(inputs, wrap::run, Wrapper.numTimeSteps);
            //UnaryOpVertex<RandomFactory,Integer[]> box = new Unar<>( random, wrap::run )
            println("Initialising black box model")
            //val box = UnaryOpLambda<VertexBackedRandomGenerator, Array<Int>>(random, Function<VertexBackedRandomGenerator, Array<Int>> { run(it) })
            //val box = UnaryOpLambda<RandomFactoryVertex, Array<Int?>>(Vertex<random!>!, Function<RandomFactoryVertex, Array<Int?>> { run(it) })
            val box = UnaryOpLambda<VertexBackedRandomGenerator, Array<Int?>>(random, Function<VertexBackedRandomGenerator, Array<Int?>> { run(it) })
            // This is the list of random numbers that are fed into model (similar to drawing from a distribution,
            // but they're pre-defined in randSource)

            // Observe the truth data plus some noise?
            if (observe) {
                println("Observing truth data. Adding noise with standard dev: $sigmaNoise")
                for (i in 0 until numTimeSteps) {
                    if (i % obInterval == 0) {
                        // output is the ith element of the model output (from box)
                        val output = IntegerArrayIndexingVertex(box, i)
                        // output with a bit of noise. Lower sigma makes it more constrained.
                        val noisyOutput = GaussianVertex(CastDoubleVertex(output), sigmaNoise)
                        // Observe the output
                        noisyOutput.observe(truth[i]!!.toDouble()) //.toDouble().scalar());
                    }
                }
            } else {
                println("Not observing truth data")
            }

            println("Creating BayesNet")
            val testNet = BayesianNetwork(box.connectedGraph)

            //GraphvizKt.toGraphvizString(box.getConnectedGraph());
            println(testNet.toGraphvizString())



            // Workaround for too many evaluations during sample startup
            random.setAndCascade(random.value)

            // Sample: feed each randomNumber in and run the model
            println("Sampling")
            val sampler = MetropolisHastings.getPosteriorSamples(testNet, Arrays.asList(box), numSamples)

            // Interrogate the samples

            // Get the number of people per iteration (an array of IntegerTensors) for each sample
            val samples = sampler.drop(dropSamples).downSample(downSample).get(box).asList()

            return samples
        }

        @JvmStatic
        fun main(args: Array<String>) {
            var samples: List<Array<Int?>>
            var observe: Boolean?

            println("Starting. Number of iterations: $numTimeSteps")

            // Make truth data
            println("Making truth data")
            val truthRandom = VertexBackedRandomGenerator(numRandomDoubles, 0, 0)
            val truth = Wrapper.run(truthRandom)

            println("Random values - Truth:\nMu")

            // Results without observations of truth data
            observe = false
            samples = keanu(truth, observe, 0)
            writeResults(samples, truth, observe, 0)


            val obIntervals = intArrayOf(1, 5, 10, 50, 100)

            for (i in obIntervals.indices) {
                observe = true
                samples = keanu(truth, observe, obIntervals[i])
                writeResults(samples, truth, observe, obIntervals[i])
            }
        }
    }


}
