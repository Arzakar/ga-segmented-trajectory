package org.klimashin.ga.segmented.trajectory.domain.model;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.TargetState;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.CommandProfile;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import org.aspectj.weaver.ast.Or;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Environment {

    final CelestialBody centralBody;
    final Map<CelestialBodyName, CelestialBody> celestialBodies;
    final Spacecraft spacecraft;

    long time;

    public long getCurrentTime() {
        return time;
    }

    public void incrementTime(long deltaTime) {
        time += deltaTime;
    }
}
