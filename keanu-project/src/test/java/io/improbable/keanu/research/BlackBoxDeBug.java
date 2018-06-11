package io.improbable.keanu.research;

import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.SmoothUniformVertex;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;

public class BlackBoxDeBug {



    public static void main(String[] args) {

        Double mTarget = 4.5;
        Double cTarget = -3.0;
        List<Double> xPoints = Arrays.asList(3.5, 4.2, 9.8, 3.6, 6.3, 9.8, -2.5, -5.7);
        ArrayList<DoubleVertex> inputs = new ArrayList<>(xPoints.size() + 2);
        // Priors on gradient and intercept
        inputs.add(new SmoothUniformVertex(-10.0, 10.0));
        inputs.add(new SmoothUniformVertex(-10.0, 10.0));
        // alternating 'x' and 'y' values
        for (Double xPoint : xPoints) {
            inputs.add(new ConstantDoubleVertex(xPoint));
            inputs.add(new ConstantDoubleVertex(mTarget * xPoint + cTarget));
        }

        Double mTest = mTarget + 0.8;
        Double cTest = cTarget + 10.1;

        // set the inputs to see if we get the expected output
        inputs.get(0).setValue(mTest);
        inputs.get(1).setValue(cTest);

        BlackBox bb = new BlackBox(inputs, BlackBoxInferenceTest::model, 1);

        bb.doubleOutputs.get(0).lazyEval();


        MultivariateFunction testFunction = new MultivariateFunction() {
            @Override
            public double value(double[] doubles) {
                inputs.get(0).setAndCascade(doubles[0]);
                inputs.get(1).setAndCascade(doubles[1]);
                //bb.doubleOutputs.get(0).lazyEval();
                return bb.doubleOutputs.get(0).getValue().scalar();
            }
        };

        double[] mins = new double[2];
        mins[0] = -10.0;
        mins[1] = -10.0;
        double[] maxs = new double[2];
        maxs[0] = 10.0;
        maxs[1] = 10.0;
        double[] starts = new double[2];
        starts[0] = 0.0;
        starts[1] = 0.0;

        BOBYQAOptimizer optimizer1 = new BOBYQAOptimizer(5);
        PointValuePair pvp = optimizer1.optimize(
            new MaxEval(100000),
            new ObjectiveFunction(testFunction),
            new SimpleBounds(mins, maxs),
            MINIMIZE,
            new InitialGuess(starts)
        );

        System.out.println("BOBYQA output");
        System.out.println(pvp.getPoint()[0]);
        System.out.println(pvp.getPoint()[1]);

        System.out.println(bb.getConnectedGraph().size());

        System.out.println(bb.doubleOutputs.get(0).getValue().scalar());

        System.out.println(BlackBoxInferenceTest.fitness(mTest, cTest));


    }
}
