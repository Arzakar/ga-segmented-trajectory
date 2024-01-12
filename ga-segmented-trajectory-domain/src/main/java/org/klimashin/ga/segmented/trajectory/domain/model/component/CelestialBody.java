package org.klimashin.ga.segmented.trajectory.domain.model.component;

import static org.klimashin.ga.segmented.trajectory.domain.model.common.Physics.G;

import org.klimashin.ga.segmented.trajectory.domain.model.common.Orbits;
import org.klimashin.ga.segmented.trajectory.domain.model.common.Physics;
import org.klimashin.ga.segmented.trajectory.domain.model.method.FixedPointIterationMethod;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
import java.util.Optional;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CelestialBody implements MassParticle {

    final Orbit orbit;

    @EqualsAndHashCode.Include
    final CelestialBodyName name;

    final double mass;

    String textName;

    public CelestialBody move(long deltaTime) {
        var meanMotion = this.getMeanMotion();
        var eccentricity = this.orbit.getEccentricity();

        var currentEccentricAnomaly = Orbits.extractEccentricAnomaly(orbit);
        var timeFromPerihelionEpoch = Math.round((currentEccentricAnomaly - eccentricity * Math.sin(currentEccentricAnomaly)) / meanMotion);

        var nextEccentricAnomaly = this.getEccentricAnomaly(timeFromPerihelionEpoch + deltaTime);
        var nextTrueAnomaly = Orbits.calculateTrueAnomaly(nextEccentricAnomaly, eccentricity);
        orbit.setTrueAnomaly(nextTrueAnomaly);

        return this;
    }

    public Point getPosition() {
        return Optional.ofNullable(this.orbit)
                .map(Orbit::getPosition)
                .orElseGet(() -> Point.of(0, 0));
    }

    public Vector getSpeed() {
        if (Objects.nonNull(this.orbit)) {
            var rate = Math.sqrt(G * orbit.getAttractingBody().getMass() / orbit.getFocalParameter());
            var radialSpeed = rate * orbit.getEccentricity() * Math.sin(orbit.getTrueAnomaly());
            var transversalSpeed = rate * (1 + orbit.getEccentricity() * Math.cos(orbit.getTrueAnomaly()));

            return Vector.of(radialSpeed, transversalSpeed).rotate(orbit.getTrueAnomaly());
        }

        return Vector.of(0, 0);
    }

    public double getMeanMotion() {
        return Math.sqrt(Physics.gravitationParameter(mass, orbit.getAttractingBody().getMass())
                / Math.pow(orbit.getSemiMajorAxis(), 3));
    }

    public double getMeanAnomaly(long currentTime) {
        return getMeanMotion() * (currentTime - orbit.getZeroEpoch());
    }

    public double getEccentricAnomaly(long currentTime) {
        if (currentTime == orbit.getZeroEpoch()) {
            return 0;
        }

        var meanAnomaly = getMeanAnomaly(currentTime);
        return new FixedPointIterationMethod(variable -> orbit.getEccentricity() * Math.sin(variable) + meanAnomaly, 0.000001)
                .getSolution(meanAnomaly);
    }
}
