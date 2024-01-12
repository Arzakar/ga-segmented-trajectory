package org.klimashin.ga.segmented.trajectory.domain.model.component.profile;

import org.klimashin.ga.segmented.trajectory.domain.model.component.MassParticle;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Vectors;
import org.klimashin.ga.segmented.trajectory.domain.util.component.ComparablePair;
import org.klimashin.ga.segmented.trajectory.domain.util.component.LongPair;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FixedVectorDeviationCommandProfile implements CommandProfile {

    MassParticle startVectorObject;
    MassParticle endVectorObject;
    SortedMap<LongPair, Double> intervals = new TreeMap<>(ComparablePair::compareByLeftTo);

    public FixedVectorDeviationCommandProfile(MassParticle startVectorObject,
                                              MassParticle endVectorObject,
                                              Map<LongPair, Double> intervals) {
        this.startVectorObject = startVectorObject;
        this.endVectorObject = endVectorObject;
        this.intervals.putAll(intervals);

        validateIntervals(intervals);
    }

    @Override
    public Vector getThrustForceDirection(final long currentTime) {
        var angle = intervals.keySet().stream()
                .filter(pair -> currentTime >= pair.getLeft() && currentTime < pair.getRight())
                .findFirst()
                .or(() -> currentTime >= intervals.lastKey().getRight()
                        ? Optional.of(intervals.lastKey())
                        : Optional.empty())
                .map(intervals::get)
                .orElseThrow(() -> new RuntimeException(String.format("""
                        В таблице временных интервалов управления отсутствует интервал,
                        содержащий текущее время, равное %d
                        """, currentTime)));

        return Vectors.between(startVectorObject.getPosition(), endVectorObject.getPosition())
                .toUnit()
                .rotate(angle);
    }

    @Override
    public Vector getThrustForce(double thrust, long currentTime) {
        return this.getThrustForceDirection(currentTime).multiply(thrust);
    }

    private void validateIntervals(Map<LongPair, Double> intervals) {
        var pairs = intervals.keySet().stream()
                .sorted(LongPair::compareByLeftTo)
                .toList();

        for (int i = 0; i < pairs.size() - 1; i++) {
            var leftBound = pairs.get(i);
            var rightBound = pairs.get(i + 1);

            if (leftBound.getRight() > rightBound.getLeft()) {
                throw new IllegalArgumentException(String.format("""
                        Ошибка в таблице временных интервалов управления.
                        Конечная граница интервала %d равна %d и больше начальной границы
                        интервала %d, которая равна %d.
                        """, i, leftBound.getRight(), i + 1, rightBound.getLeft()));
            }
        }
    }
}
