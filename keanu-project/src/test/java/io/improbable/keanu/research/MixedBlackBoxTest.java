package io.improbable.keanu.research;

import io.improbable.keanu.randomfactory.RandomFactory;

import java.util.ArrayList;

public class MixedBlackBoxTest {

    public static MixedModelIO model(ArrayList<Integer> integerInputs,
                                     ArrayList<Double> doubleInputs,
                                     RandomFactory<Double> random) {

        ArrayList<Integer> intsOut = new ArrayList<>();
        ArrayList<Double> dubsOut = new ArrayList<>();

        Integer intTotal = 0;
        for (int i=0; i<integerInputs.size(); i++) {
            intTotal += integerInputs.get(i);
        }
        intsOut.add(intTotal);

        Double dubTotal = 0.0;
        for (int i=0; i<doubleInputs.size(); i++) {
            dubTotal += doubleInputs.get(i);
        }
        dubsOut.add(dubTotal);

        MixedModelIO output = new MixedModelIO(intsOut, dubsOut);
        return output;
    }
}
