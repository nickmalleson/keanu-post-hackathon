package io.improbable.keanu.research;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.VertexSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.FitnessFunction;
import io.improbable.keanu.algorithms.variational.GradientOptimizer;
import io.improbable.keanu.algorithms.variational.NonGradientOptimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.randomfactory.RandomFactory;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.tensor.dbl.ScalarDoubleTensor;
import io.improbable.keanu.vertices.ConstantVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.SmoothUniformVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.UniformVertex;
import io.improbable.vis.Vizer;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.math3.optim.nonlinear.scalar.GoalType.MINIMIZE;

public class BlackBoxInferenceTest {

    public static DoubleTensor[] model(DoubleTensor[] inputs, RandomFactory<Double> random) {
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
            xPoints.add(it.next());
            yPoints.add(it.next());
        }

        Double SumOfSquaredError = 0.0;

        for (int i = 0; i < yPoints.size(); i++) {
            Double yModelled = m * xPoints.get(i) + c;
            Double error = yPoints.get(i) - yModelled;
            SumOfSquaredError += error * error;
        }

        Double MSE = SumOfSquaredError / yPoints.size();
        double[] RMSE = new double[1];
        RMSE[0] = Math.sqrt(MSE);

        output[0] = DoubleTensor.create(RMSE);

        return output;
    }

    public static Double fitness(Double m, Double c) {

        List<Double> xPoints = Arrays.asList(3.5, 4.2, 9.8, 3.6, 6.3, 9.8, -2.5, -5.7);
        Double mTarget = 4.5;
        Double cTarget = -3.0;
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

    public static void main(String[] args) {
        Double mTarget = 4.5;
        Double cTarget = -3.0;
        List<Double> xPoints = Arrays.asList(3.5, 4.2, 9.8, 3.6, 6.3, 9.8, -2.5, -5.7);

        ArrayList<DoubleVertex> inputs = new ArrayList<>(xPoints.size() + 2);
        // Priors on gradient and intercept
        inputs.add(new SmoothUniformVertex(-10.0, 10.0));
        inputs.add(new SmoothUniformVertex(-10.0, 10.0));
        inputs.get(0).setAndCascade(0.0);
        inputs.get(1).setAndCascade(0.0);
        // alternating 'x' and 'y' values
        for (Double xPoint : xPoints) {
            inputs.add(new ConstantDoubleVertex(xPoint));
            inputs.add(new ConstantDoubleVertex(mTarget * xPoint + cTarget));
        }

        BlackBox box = new BlackBox(inputs, BlackBoxInferenceTest::model, 1);
        box.fuzzyObserve(0, 0.0, 0.5);
        BayesianNetwork testNet = new BayesianNetwork(box.getConnectedGraph());

        NonGradientOptimizer optimizer = new NonGradientOptimizer(testNet);
        optimizer.maxAPosteriori(1000000, 10.0);

        System.out.println("size " + box.getConnectedGraph().size());
        System.out.println(inputs.get(0).getValue().scalar());
        System.out.println(inputs.get(1).getValue().scalar());

        // OOOOOKKKK SO LET'S TRY IT USING KEANU PROPERLY //

        double[] xs = new double[xPoints.size()];
        double[] ys = new double[xPoints.size()];
        for (int i=0; i<xPoints.size(); i++) {
            xs[i] = xPoints.get(i);
            ys[i] = xPoints.get(i)*mTarget + cTarget;
        }
        DoubleTensor xData = DoubleTensor.create(xs);
        DoubleTensor yData = DoubleTensor.create(ys);

        // Linear Regression
        DoubleVertex m = new GaussianVertex(0.0, 10.0);
        DoubleVertex b = new GaussianVertex(0.0, 10.0);
        DoubleVertex x = ConstantVertex.of(xData);
        DoubleVertex y = new GaussianVertex(x.multiply(m).plus(b), 5.0);
        y.observe(yData);

        BayesianNetwork bayesNet = new BayesianNetwork(m.getConnectedGraph());
        NonGradientOptimizer optimizer2 = new NonGradientOptimizer(bayesNet);
        optimizer2.maxAPosteriori(1000000, 10.0);

        System.out.println(m.getValue().scalar());
        System.out.println(b.getValue().scalar());

        // ONCE MORE BUT USING BOBYQA DIRECTLY //

        MultivariateFunction testFunction = new MultivariateFunction() {
            @Override
            public double value(double[] doubles) {
                return fitness(doubles[0], doubles[1]);
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
        System.out.println(pvp.getPoint()[0]);
        System.out.println(pvp.getPoint()[1]);
    }








//        VertexSamples<ScalarDoubleTensor> samples0 = testMet.get(inputs.get(0).getId());
//        VertexSamples<ScalarDoubleTensor> samples1 = testMet.get(inputs.get(1).getId());
//
//        ArrayList<Double> test = new ArrayList<>();
//        Double Onetotal = 0.0;
//        Double Zerototal = 0.0;
//        for (int i=0; i<samples1.asList().size(); i++) {
//            test.add(samples1.asList().get(i).scalar() + samples0.asList().get(i).scalar());
//            Onetotal += samples1.asList().get(i).scalar();
//            Zerototal += samples0.asList().get(i).scalar();
//        }
//        Double OneMean = Onetotal/samples1.asList().size();
//        Double TwoMean = Zerototal/samples1.asList().size();
//
//        System.out.println("Input One Mean: " + OneMean + " Input Two Mean: " + TwoMean);


}

//        NetworkSamples testMet = MetropolisHastings.getPosteriorSamples(testNet, inputs, 100000).drop(10000);
//
//        ArrayList<Double> test = new ArrayList<>();
//        VertexSamples<ScalarDoubleTensor> samples0 = testMet.get(inputs.get(0).getId());
//        for (int i = 0; i < samples0.asList().size(); i++) {
//            test.add(samples0.asList().get(i).scalar());
//        }
//
//        Vizer.histogram(test);
