package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;

public class BlackBoxLinearRegressionTest {

    private double mTarget = 4.5;
    private double cTarget = -3.0;
    private List<Double> xPoints = generateXData(100);

    @Test
    public void blackBoxGetsSameResultsAsKeanuNative() {
        Pair<Double, Double> bobyquaResult = regressWithBOBYQA();
        Pair<Double, Double> keanuNativeResult = regressWithKeanuNative();
        Pair<Double, Double> blackBoxResult = regressWithBlackBox();

        Assert.assertEquals(mTarget, bobyquaResult.getFirst(), 1e-5);
        Assert.assertEquals(cTarget, bobyquaResult.getSecond(), 1e-5);
        Assert.assertEquals(mTarget, keanuNativeResult.getFirst(), 1e-2);
        Assert.assertEquals(cTarget, keanuNativeResult.getSecond(), 1e-2);
        Assert.assertEquals(mTarget, blackBoxResult.getFirst(), 1e-2);
        Assert.assertEquals(cTarget, blackBoxResult.getSecond(), 1e-2);
    }

    private List<Double> generateXData(int N) {
        Random r = new Random(1);
        List<Double> xs = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            xs.add((r.nextDouble() - 0.5) * 50.0);
        }

        return xs;
    }

    private Pair<Double, Double> regressWithBOBYQA() {
        MultivariateFunction testFunction = (double[] doubles) -> fitness(doubles[0], doubles[1]);

        double[] mins = new double[2];
        mins[0] = -10.0;
        mins[1] = -10.0;
        double[] maxs = new double[2];
        maxs[0] = 10.0;
        maxs[1] = 10.0;
        double[] starts = new double[2];
        starts[0] = 0.0;
        starts[1] = 0.0;

        BOBYQAOptimizer optimizer = new BOBYQAOptimizer(5);
        PointValuePair pvp = optimizer.optimize(
            new MaxEval(100000),
            new ObjectiveFunction(testFunction),
            new SimpleBounds(mins, maxs),
            MINIMIZE,
            new InitialGuess(starts)
        );

        double m = pvp.getPoint()[0];
        double c = pvp.getPoint()[1];
        return new Pair<>(m, c);
    }

    public Double fitness(Double m, Double c) {
        ArrayList<Double> yPoints = new ArrayList<>();
        for (Double xPoint : xPoints) {
            yPoints.add(mTarget * xPoint + cTarget);
        }
        Double SumOfSquaredError = 0.0;
        for (int i = 0; i < yPoints.size(); i++) {
            Double yModelled = m * xPoints.get(i) + c;
            Double error = yPoints.get(i) - yModelled;
            SumOfSquaredError += error * error;
        }
        Double MSE = SumOfSquaredError / yPoints.size();
        return MSE;
    }

    public Pair<Double, Double> regressWithKeanuNative() {
        double[] xs = new double[xPoints.size()];
        double[] ys = new double[xPoints.size()];
        for (int i = 0; i < xPoints.size(); i++) {
            double x = xPoints.get(i);
            xs[i] = x;
            ys[i] = x * mTarget + cTarget;
        }

        DoubleTensor xData = DoubleTensor.create(xs);
        DoubleTensor yData = DoubleTensor.create(ys);

        DoubleVertex m = new GaussianVertex(0.0, 10.0);
        DoubleVertex c = new GaussianVertex(0.0, 10.0);
        DoubleVertex x = ConstantVertex.of(xData);
        DoubleVertex y = new GaussianVertex(x.multiply(m).plus(c), 5.0);
        y.observe(yData);

        BayesianNetwork bayesNet = new BayesianNetwork(m.getConnectedGraph());
        NonGradientOptimizer optimizer = new NonGradientOptimizer(bayesNet);
        optimizer.maxAPosteriori(10000, 50.0);

        return new Pair<>(m.getValue().scalar(), c.getValue().scalar());
    }

    public Pair<Double, Double> regressWithBlackBox() {
        ArrayList<DoubleVertex> inputs = new ArrayList<>(xPoints.size() + 2);

        DoubleVertex m = new GaussianVertex(0.0, 10.0);
        DoubleVertex c = new GaussianVertex(0.0, 10.0);
        m.setAndCascade(0.0);
        c.setAndCascade(0.0);
        inputs.add(m);
        inputs.add(c);

        for (Double xPoint : xPoints) {
            inputs.add(new ConstantDoubleVertex(xPoint));
            inputs.add(new ConstantDoubleVertex(mTarget * xPoint + cTarget));
        }

        BlackBox box = new BlackBox(inputs, this::blackBoxModel,1);
        box.fuzzyObserve(0, 0.0, 0.5);

        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        NonGradientOptimizer optimizer = new NonGradientOptimizer(testNet);
        optimizer.maxAPosteriori(1000000, 10.0);

        return new Pair<>(m.getValue().scalar(), c.getValue().scalar());
    }

    private DoubleTensor[] blackBoxModel(DoubleTensor[] inputs, RandomFactory<Double> random) {
        DoubleTensor[] output = new DoubleTensor[1];
        ArrayList<Double> inputArray = new ArrayList<>(inputs.length);
        for (DoubleTensor input : inputs) {
            inputArray.add(input.scalar());
        }
        Iterator<Double> it = inputArray.iterator();
        Double m = it.next();
        Double c = it.next();
        ArrayList<Double> xPoints = new ArrayList<>();
        ArrayList<Double> yPoints = new ArrayList<>();
        while (it.hasNext()) {
            double x = it.next();
            double y = it.next();
            xPoints.add(x);
            yPoints.add(y);
        }

        Double SumOfSquaredError = 0.0;

        for (int i = 0; i < yPoints.size(); i++) {
            Double yActual = m * xPoints.get(i) + c;
            Double yExpected = yPoints.get(i);
            Double error = yActual - yExpected;
            SumOfSquaredError += error * error;
        }

        Double MSE = SumOfSquaredError / yPoints.size();
        double[] RMSE = new double[1];
        RMSE[0] = Math.sqrt(MSE);

        output[0] = DoubleTensor.create(RMSE);

        return output;
    }
}