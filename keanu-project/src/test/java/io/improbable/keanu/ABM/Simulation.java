package io.improbable.keanu.ABM;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesNet;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.research.BlackBox;
import io.improbable.keanu.research.VertexBackedRandomFactory;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;

import java.util.ArrayList;

public class Simulation {

    Agent[][] grid;
    VertexBackedRandomFactory random;
    int timesteps = 100;
    int numberOfPredators = 2;
    int numberOfPrey = 30;

    public Simulation(int XSIZE, int YSIZE, VertexBackedRandomFactory random) {
        grid = new Agent[XSIZE][YSIZE];
        this.random = random;
    }

    public Double[] model(Double[] inputs, RandomFactory<Double> random) {


        // TODO... fill in here
        run(timesteps);
        Double[] outputs = new Double[10];
        return outputs;
    }

    public void initialiseSimulation() {
        for (int i=0; i<numberOfPredators; i++) {

        }
    }

    private void step() {
        ArrayList<Agent> tempAgentCollection = new ArrayList<>();
        for (Agent[] subset: grid) {
            for (Agent agent: subset) {
                tempAgentCollection.add(agent);
            }
        }
        for (Agent agent: tempAgentCollection) {
            agent.step();
        }
    }

    private void run(int noSteps) {
        for (int i=0; i<noSteps; i++) {
            step();
        }
    }

    public Agent getXY(int xLocation, int yLocation) {
        return grid[(xLocation+grid.length)%grid.length][(yLocation+grid[0].length)%grid[0].length];
    }

    public void spawnPrey(int startX, int startY) {
        grid[startX][startY] = new Prey(this, startX, startY);
    }

    public void spawnPreditor(int startX, int startY) {
        grid[startX][startY] = new Predator(this, startX, startY);
    }

    public static void main (String[] args) {

        Simulation simulation = new Simulation(10, 10, new VertexBackedRandomFactory(10, 10));

        ArrayList<DoubleVertex> inputs = new ArrayList<>(2);
        inputs.add(new GaussianVertex(5.5, 3.0));
        inputs.add(new GaussianVertex(6.1, 2.0));

        BlackBox box = new BlackBox(inputs, simulation::model, 2);

        box.fuzzyObserve(1, 14.0, 0.5);

        BayesNet simulationNet = new BayesNet(box.getConnectedGraph());

        NetworkSamples samples = MetropolisHastings.getPosteriorSamples(simulationNet, inputs, 1000);
    }
}