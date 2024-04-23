package org.klimashin.ga.segmented.trajectory.domain.util.common;


import org.klimashin.ga.segmented.trajectory.domain.util.component.Pair;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Segment;

import java.util.Random;

public class PairRandom extends Random {

    public Double nextDouble(Segment<Double> segment) {
        return segment.getLeft().equals(segment.getRight())
                ? segment.getLeft()
                : super.nextDouble(segment.getLeft(), segment.getRight());
    }

    public Integer nextInt(Pair<Integer, Integer> pair) {
        return pair.getLeft().equals(pair.getRight())
                ? pair.getLeft()
                : super.nextInt(pair.getLeft(), pair.getRight() + 1);
    }

    public Double nextDouble(Pair<Double, Double> pair) {
        return pair.getLeft().equals(pair.getRight())
                ? pair.getLeft()
                : super.nextDouble(pair.getLeft(), pair.getRight());
    }
}
