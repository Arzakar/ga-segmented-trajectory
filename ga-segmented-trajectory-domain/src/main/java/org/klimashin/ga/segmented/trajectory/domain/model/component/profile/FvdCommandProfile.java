package org.klimashin.ga.segmented.trajectory.domain.model.component.profile;

import org.klimashin.ga.segmented.trajectory.domain.model.component.MassParticle;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Vectors;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FvdCommandProfile implements CommandProfile {

    MassParticle startVectorObject;
    MassParticle endVectorObject;
    SortedMap<Long, Double> endIntervalsByDeviations = new TreeMap<>(Long::compareTo);

    public FvdCommandProfile(MassParticle startVectorObject,
                             MassParticle endVectorObject,
                             Map<Long, Double> endIntervalsByDeviations) {
        this.startVectorObject = startVectorObject;
        this.endVectorObject = endVectorObject;
        this.endIntervalsByDeviations.putAll(endIntervalsByDeviations);

        validateIntervals(endIntervalsByDeviations);
    }

    @Override
    public Vector getThrustForceDirection(final long currentTime) {
        var angle = endIntervalsByDeviations.keySet().stream()
                .filter(bond -> currentTime < bond)
                .sorted()
                .findFirst()
                .or(() -> currentTime >= endIntervalsByDeviations.lastKey()
                        ? Optional.of(endIntervalsByDeviations.lastKey())
                        : Optional.empty())
                .map(endIntervalsByDeviations::get)
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

    private void validateIntervals(Map<Long, Double> endIntervalsByDeviations) {
        var endIntervals = endIntervalsByDeviations.keySet().stream()
                .sorted(Long::compareTo)
                .toList();

        for (int i = 0; i < endIntervals.size() - 1; i++) {
            var currentBond = endIntervals.get(i);
            var nextBond = endIntervals.get(i + 1);

            if (currentBond.equals(nextBond)) {
                throw new IllegalArgumentException(String.format("""
                        Ошибка в таблице временных интервалов управления.
                        Граница %d равна границе %d и имеет значение %d.
                        """, i, i + 1, currentBond));
            }
        }
    }

    public static class FvdCommandProfileBuilder {

        private SortedMap<Long, Double> endIntervalsByDeviations;

        public FvdCommandProfileBuilder endIntervalsByDeviations(SortedMap<Long, Double> endIntervalsByDeviations) {
            this.endIntervalsByDeviations = endIntervalsByDeviations;
            return this;
        }

        public FvdCommandProfile build() {
            return new FvdCommandProfile(this.startVectorObject, this.endVectorObject, this.endIntervalsByDeviations);
        }
    }
}
