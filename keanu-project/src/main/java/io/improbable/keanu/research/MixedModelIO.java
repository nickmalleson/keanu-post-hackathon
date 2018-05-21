package io.improbable.keanu.research;

import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;

import java.util.ArrayList;

public class MixedModelIO {

    public Vertex<ArrayList<Integer>> integersIn;
//    public Vertex<ArrayList<Integer>> integersOut;
    public Vertex<Integer[]> integersOut;
    public ArrayList<IntegerVertex> listOfIntegerVertexOutputs = new ArrayList<>();
    public Integer expectedNumberOfIntegersOut;
    public Vertex<ArrayList<Double>> doublesIn;
//    public Vertex<ArrayList<Double>> doublesOut;
    public Vertex<Double[]> doublesOut;
    public ArrayList<DoubleVertex> listOfDoubleVertexOutputs = new ArrayList<>();
    public Integer expectedNumberOfDoublesOut;

    public MixedModelIO(ArrayList<IntegerVertex> integersIn,
                        ArrayList<DoubleVertex> doublesIn,
                        Integer expectedNumberOfIntegersOut,
                        Integer expectedNumberOfDoublesOut) {
        this.integersIn = new ReduceVertex<>(integersIn, (ArrayList<Integer> in) -> {
            ArrayList<Integer> out = new ArrayList<>();
            for (int i=0; i<in.size(); i++) {
                out.add(in.get(i));
            }
            return out;
        });
        this.doublesIn = new ReduceVertex<>(doublesIn, (ArrayList<Double> in) -> {
            ArrayList<Double> out = new ArrayList<>();
            for (int i=0; i<in.size(); i++) {
                out.add(in.get(i));
            }
            return out;
        });
        this.integersOut = null;
        this.doublesOut = null;
        this.expectedNumberOfIntegersOut = expectedNumberOfIntegersOut;
        this.expectedNumberOfDoublesOut = expectedNumberOfDoublesOut;
    }

    public MixedModelIO(Vertex<Integer[]> integersOut,
                        Vertex<Double[]> doublesOut) {
        this.integersOut = integersOut;
        this.doublesOut = doublesOut;

        this.integersIn = null;
        this.doublesIn = null;
        this.expectedNumberOfIntegersOut = null;
        this.expectedNumberOfDoublesOut = null;
    }
}
