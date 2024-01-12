package org.klimashin.ga.segmented.trajectory.domain.util.component;

public class DoublePair extends ComparablePair<Double, Double> {

    public static DoublePair of(Double left, Double right) {
        return new DoublePair(left, right);
    }

    protected DoublePair(Double left, Double right) {
        super(left, right);
    }
}
