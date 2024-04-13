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

    long time;

    CelestialBody centralBody;
    Map<CelestialBodyName, CelestialBody> celestialBodies;

    Spacecraft spacecraft;
    Orbit resultOrbit;
    Orbit resultLeftOrbit;
    Orbit resultRightOrbit;

    CommandProfile commandProfile;
    TargetState targetState;

    public long getCurrentTime() {
        return time;
    }

    public void incrementTime(long deltaTime) {
        time += deltaTime;
    }

    public Environment injectResultOrbit(Orbit orbit) {
        this.resultOrbit = orbit;
        return this;
    }

    public Environment injectResultOrbits(Orbit leftOrbit, Orbit rightOrbit) {
        this.resultLeftOrbit = leftOrbit;
        this.resultRightOrbit = rightOrbit;
        return this;
    }

    public Environment copy() {
        return Environment.builder()
                .centralBody(CelestialBody.builder()
                        .mass(centralBody.getMass())
                        .build())
                .celestialBodies(celestialBodies.values().stream()
                        .map(celestialBody -> CelestialBody.builder()
                                .mass(celestialBody.getMass())
                                .orbit(Orbit.builder()
                                        .attractingBody(celestialBody.getOrbit().getAttractingBody())
                                        .semiMajorAxis(celestialBody.getOrbit().getSemiMajorAxis())
                                        .eccentricity(celestialBody.getOrbit().getEccentricity())
                                        .inclination(celestialBody.getOrbit().getInclination())
                                        .longitudeAscNode(celestialBody.getOrbit().getLongitudeAscNode())
                                        .perihelionArgument(celestialBody.getOrbit().getPerihelionArgument())
                                        .trueAnomaly(celestialBody.getOrbit().getTrueAnomaly())
                                        .zeroEpoch(celestialBody.getOrbit().getZeroEpoch())
                                        .build())
                                .name(celestialBody.getName())
                                .build()
                        ).collect(Collectors.toMap(CelestialBody::getName, Function.identity())))
                .spacecraft(Spacecraft.builder()
                        .mass(spacecraft.getMass())
                        .position(spacecraft.getPosition().copy())
                        .speed(spacecraft.getSpeed().copy())
                        .fuelMass(spacecraft.getFuelMass())
                        .thrust(spacecraft.getThrust())
                        .fuelConsumption(spacecraft.getFuelConsumption())
                        .build())
                .time(getCurrentTime())
                .build();
    }
}
