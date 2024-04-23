package org.klimashin.ga.segmented.trajectory.domain.model.component.profile;

import org.klimashin.ga.segmented.trajectory.domain.model.component.Particle;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Vectors;
import org.klimashin.ga.segmented.trajectory.domain.util.component.LongSegment;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FvdCommandProfile implements CommandProfile {

    Particle startVectorObject;
    Particle endVectorObject;
    SortedMap<LongSegment, Double> intervalsByDeviations = new TreeMap<>(LongSegment::compareByLeftTo);

    public FvdCommandProfile(Particle startVectorObject,
                             Particle endVectorObject,
                             Map<LongSegment, Double> intervalsByDeviations) {
        this.startVectorObject = startVectorObject;
        this.endVectorObject = endVectorObject;
        this.intervalsByDeviations.putAll(intervalsByDeviations);

        validateIntervals(intervalsByDeviations);
    }

    @Override
    public Vector getThrustForceDirection(long currentTime) {
        return intervalsByDeviations.keySet().stream()
                .filter(segment -> segment.isInclude(currentTime))
                .findFirst()
                .map(intervalsByDeviations::get)
                .map(angle -> Vectors.between(startVectorObject.getPosition(), endVectorObject.getPosition())
                        .toUnit()
                        .rotate(angle))
                .orElse(Vector.zero());
    }

    @Override
    public Vector getThrustForce(double thrust, long currentTime) {
        return this.getThrustForceDirection(currentTime)
                .multiply(thrust);
    }

    private void validateIntervals(Map<LongSegment, Double> intervalsByDeviations) {
        var intervals = intervalsByDeviations.keySet().stream()
                .sorted(LongSegment::compareByLeftTo)
                .toList();

        for (int i = 0; i < intervals.size() - 1; i++) {
            var currentSegment = intervals.get(i);
            var nextSegment = intervals.get(i + 1);

            if (currentSegment.isIntersectWith(nextSegment)) {
                throw new IllegalArgumentException(String.format("""
                        Ошибка в таблице временных интервалов управления.
                        Правая граница интервала %d равна или больше левой границы интервала %d.
                        %d >= %d.
                        """, i, i + 1, currentSegment.getRight(), nextSegment.getLeft()));
            }
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FvdCommandProfileBuilder {

        Particle startVectorObject;
        Particle endVectorObject;
        Map<LongSegment, Double> intervalsByDeviations;

        public FvdCommandProfileBuilder startVectorObject(Particle startVectorObject) {
            this.startVectorObject = startVectorObject;
            return this;
        }

        public FvdCommandProfileBuilder endVectorObject(Particle endVectorObject) {
            this.endVectorObject = endVectorObject;
            return this;
        }

        public FvdCommandProfileBuilder intervalsByDeviations(Map<LongSegment, Double> intervalsByDeviations) {
            this.intervalsByDeviations = intervalsByDeviations;
            return this;
        }

        public FvdCommandProfile build() {
            return new FvdCommandProfile(this.startVectorObject, this.endVectorObject, this.intervalsByDeviations);
        }
    }

    public static FvdCommandProfileBuilder builder() {
        return new FvdCommandProfileBuilder();
    }
}
