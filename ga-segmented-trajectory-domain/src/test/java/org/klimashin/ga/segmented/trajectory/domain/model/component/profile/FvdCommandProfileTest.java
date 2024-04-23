package org.klimashin.ga.segmented.trajectory.domain.model.component.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.withPrecision;

import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.util.component.LongSegment;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import java.util.Map;

class FvdCommandProfileTest {

    private final EasyRandom easyRandom = new EasyRandom();

    @Test
    void create_shouldThrowException_whenSegmentsIsIntersects() {
        var startPoint = easyRandom.nextObject(Spacecraft.class);
        var endPoint = easyRandom.nextObject(Spacecraft.class);

        var intervalsByDeviations = Map.of(
                LongSegment.of(25, 50), 50d,
                LongSegment.of(0, 10), 25d,
                LongSegment.of(12, 26), 5d
        );

        var exception = catchThrowableOfType(() -> new FvdCommandProfile(startPoint, endPoint, intervalsByDeviations),
                IllegalArgumentException.class);

        assertThat(exception.getMessage()).isNotNull();
    }

    @Test
    void create_shouldCreateNewCommandProfile() {
        var startPoint = easyRandom.nextObject(Spacecraft.class);
        var endPoint = easyRandom.nextObject(Spacecraft.class);

        var intervalsByDeviations = Map.of(
                LongSegment.of(25, 50), 50d,
                LongSegment.of(0, 10), 25d,
                LongSegment.of(10, 25), 5d,
                LongSegment.of(100, 150), 45d
        );

        var commandProfile = new FvdCommandProfile(startPoint, endPoint, intervalsByDeviations);

        assertThat(commandProfile)
                .returns(startPoint, FvdCommandProfile::getStartVectorObject)
                .returns(endPoint, FvdCommandProfile::getEndVectorObject);
    }

    @Test
    void getThrustForceDirection_shouldReturnDirection() {
        var startPoint = Spacecraft.builder()
                .position(Point.of(0, 0))
                .build();
        var endPoint = Spacecraft.builder()
                .position(Point.of(10, 10))
                .build();
        var intervalsByDeviations = Map.of(
                LongSegment.of(0, 10), Math.PI / 4d,
                LongSegment.of(15, 25), 5d
        );

        var forceDirection = new FvdCommandProfile(startPoint, endPoint, intervalsByDeviations)
                .getThrustForceDirection(5);

        assertThat(forceDirection.getX()).isEqualTo(0, withPrecision(10E-6));
        assertThat(forceDirection.getY()).isEqualTo(1, withPrecision(10E-6));
    }

    @Test
    void getThrustForceDirection_shouldReturnZero_whenCurrentTimeNotInIntervals() {
        var startPoint = Spacecraft.builder()
                .position(Point.of(0, 0))
                .build();
        var endPoint = Spacecraft.builder()
                .position(Point.of(10, 10))
                .build();
        var intervalsByDeviations = Map.of(
                LongSegment.of(0, 10), Math.PI / 4d,
                LongSegment.of(15, 25), 5d
        );

        var forceDirection = new FvdCommandProfile(startPoint, endPoint, intervalsByDeviations)
                .getThrustForceDirection(12);

        assertThat(forceDirection).isEqualTo(Vector.zero());
    }

    @Test
    void getThrustForce_shouldReturnForce() {
        var startPoint = Spacecraft.builder()
                .position(Point.of(0, 0))
                .build();
        var endPoint = Spacecraft.builder()
                .position(Point.of(10, 10))
                .build();
        var intervalsByDeviations = Map.of(
                LongSegment.of(0, 10), Math.PI / 4d,
                LongSegment.of(15, 25), 5d
        );

        var forceDirection = new FvdCommandProfile(startPoint, endPoint, intervalsByDeviations)
                .getThrustForce(10, 5);

        assertThat(forceDirection.getX()).isEqualTo(0, withPrecision(10E-6));
        assertThat(forceDirection.getY()).isEqualTo(10, withPrecision(10E-6));
    }

    @Test
    void getThrustForce_shouldReturnZero_whenCurrentTimeNotInIntervals() {
        var startPoint = Spacecraft.builder()
                .position(Point.of(0, 0))
                .build();
        var endPoint = Spacecraft.builder()
                .position(Point.of(10, 10))
                .build();
        var intervalsByDeviations = Map.of(
                LongSegment.of(0, 10), Math.PI / 4d,
                LongSegment.of(15, 25), 5d
        );

        var forceDirection = new FvdCommandProfile(startPoint, endPoint, intervalsByDeviations)
                .getThrustForce(10, 12);

        assertThat(forceDirection).isEqualTo(Vector.zero());
    }
}