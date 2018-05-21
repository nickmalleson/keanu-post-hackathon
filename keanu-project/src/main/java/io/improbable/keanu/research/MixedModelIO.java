package io.improbable.keanu.research;

public class MixedModelIO {

    public Integer[] integersIn;
    public Integer[] integersOut;
    public Double[] doublesIn;
    public Double[] doublesOut;

    public MixedModelIO(Integer[] integersIn, Integer[] integersOut, Double[] doublesIn, Double[] doublesOut) {
        this.integersIn = integersIn;
        this.integersOut = integersOut;
        this.doublesIn = doublesIn;
        this.doublesOut = doublesOut;
    }
}
