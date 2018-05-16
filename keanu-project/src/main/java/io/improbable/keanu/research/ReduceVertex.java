package io.improbable.keanu.research;

import io.improbable.keanu.vertices.NonProbabilisticObservationException;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

import java.util.*;
import java.util.function.Function;

public class ReduceVertex<INPUT, T> extends NonProbabilistic<T> {

    protected final List<? extends Vertex<INPUT>> inputs;
    protected final Function<ArrayList<INPUT>, T> f;

    public ReduceVertex(Collection<? extends Vertex<INPUT>> inputs,
                        Function<ArrayList<INPUT>, T> f) {
        this.inputs = new ArrayList<>(inputs);
        this.f = f;
        setParents(inputs);

        if (inputs.size() < 2) {
            throw new IllegalArgumentException("DoubleReduceVertex should have at least two input vertices, called with " + inputs.size());
        }
    }

    public ReduceVertex(Function<ArrayList<INPUT>, T> f,
                        Vertex<INPUT>... input) {
        this(Arrays.asList(input), f);
    }

    @Override
    public T sample() {
        return applyReduce(Vertex::sample);
    }

    @Override
    public T lazyEval() {
        setValue(applyReduce(Vertex::lazyEval));
        return getValue();
    }

    @Override
    public T getDerivedValue() {
        return applyReduce(Vertex::getValue);
    }

    private T applyReduce(Function<Vertex<T>, T> mapper) {
        Iterator<? extends Vertex<INPUT>> samples = inputs.iterator();
        ArrayList<INPUT> functionInpute = new ArrayList<>(inputs.size());
        while (samples.hasNext()) {
            functionInpute.add(samples.next().getValue());
        }
        return f.apply(functionInpute);
    }

//    @Override
//    public DualNumber calculateDualNumber(Map<Vertex, DualNumber> dualNumbers) {
//        if (dualNumberSupplier != null) {
//            return dualNumberSupplier.get();
//        }
//
//        throw new UnsupportedOperationException();
//    }

    @Override
    public void observe(T value) {
        throw new NonProbabilisticObservationException();
    }

//    @Override
//    public double logPdf(T value) {
//        return this.getDerivedValue().equals(value) ? 0.0 : Double.NEGATIVE_INFINITY;
//    }

//    @Override
//    public Map<Long, DoubleTensor> dLogPdf(Double value) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    public T updateValue() {
        setValue(getDerivedValue());
        return getValue();
    }
}
