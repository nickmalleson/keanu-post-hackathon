package io.improbable.keanu.research;

import io.improbable.keanu.vertices.NonProbabilisticObservationException;
import io.improbable.keanu.vertices.Vertex;
import io.improbable.keanu.vertices.generic.nonprobabilistic.NonProbabilistic;

import java.util.*;
import java.util.function.Function;

public class ReduceVertex<INPUT, OUTPUT> extends NonProbabilistic<OUTPUT> {

    protected final List<? extends Vertex<INPUT>> inputs;
    protected final Function<ArrayList<INPUT>, OUTPUT> f;

    public ReduceVertex(Collection<? extends Vertex<INPUT>> inputs,
                        Function<ArrayList<INPUT>, OUTPUT> f) {
        this.inputs = new ArrayList<>(inputs);
        this.f = f;
        setParents(inputs);

        if (inputs.size() < 2) {
            throw new IllegalArgumentException("ReduceVertex should have at least two input vertices, called with " + inputs.size());
        }
    }

    public ReduceVertex(Function<ArrayList<INPUT>, OUTPUT> f,
                        Vertex<INPUT>... input) {
        this(Arrays.asList(input), f);
    }

    @Override
    public OUTPUT sample() {
        return applyReduce(Vertex::sample);
    }

    @Override
    public OUTPUT lazyEval() {
        setValue(applyReduce(Vertex::lazyEval));
        return getValue();
    }

    @Override
    public OUTPUT getDerivedValue() {
        return applyReduce(Vertex::getValue);
    }

    private OUTPUT applyReduce(Function<Vertex<OUTPUT>, OUTPUT> mapper) {
        Iterator<? extends Vertex<INPUT>> samples = inputs.iterator();
        ArrayList<INPUT> functionInputs = new ArrayList<>(inputs.size());
        while (samples.hasNext()) {
            functionInputs.add(samples.next().getValue());
        }
        return f.apply(functionInputs);
    }

    @Override
    public void observe(OUTPUT value) {
        throw new NonProbabilisticObservationException();
    }

    @Override
    public OUTPUT updateValue() {
        setValue(getDerivedValue());
        return getValue();
    }
}
