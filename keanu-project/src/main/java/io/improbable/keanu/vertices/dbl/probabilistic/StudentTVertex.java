package io.improbable.keanu.vertices.dbl.probabilistic;

import io.improbable.keanu.distributions.continuous.StudentT;
import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;

import java.util.HashMap;
import java.util.Map;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkTensorsMatchNonScalarShapeOrAreScalar;

public class StudentTVertex extends ProbabilisticDouble {

    private final IntegerVertex v;

    /**
     * @param tensorShape expected tensor shape
     * @param v           Degrees of Freedom
     */
    public StudentTVertex(int[] tensorShape, IntegerVertex v) {
        checkTensorsMatchNonScalarShapeOrAreScalar(tensorShape, v.getShape());
        this.v = v;
        setParents(v);
        setValue(DoubleTensor.placeHolder(tensorShape));
    }

    public StudentTVertex(int[] tensorShape, int v) {
        this(tensorShape, new ConstantIntegerVertex(v));
    }

    public StudentTVertex(IntegerVertex v) {
        this(v.getShape(), v);
    }

    public StudentTVertex(int v) {
        this(Tensor.SCALAR_SHAPE, new ConstantIntegerVertex(v));
    }

    /**
     * @return degrees of freedom (v)
     */
    public IntegerVertex getV() {
        return v;
    }

    /**
     * @param t random variable
     * @return Log of the Probability Density of t
     */
    @Override
    public double logPdf(DoubleTensor t) {
        return StudentT.logPdf(v.getValue(), t).sum();
    }

    /**
     * @param t random variable
     * @return Differential of the Log of the Probability Density of t
     */
    @Override
    public Map<Long, DoubleTensor> dLogPdf(DoubleTensor t) {
        StudentT.Diff diff = StudentT.dLogPdf(v.getValue(), t);
        Map<Long, DoubleTensor> m = new HashMap<>();
        m.put(getId(), diff.dPdt);
        return m;
    }

    /**
     * @return sample of Student T distribution
     */
    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return StudentT.sample(getShape(), v.getValue(), random);
    }
}
