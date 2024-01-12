package org.klimashin.ga.segmented.trajectory.domain.model.common;

import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Orbits {

    public double extractEccentricAnomaly(Orbit orbit) {
        return calculateEccentricAnomaly(orbit.getTrueAnomaly(), orbit.getEccentricity());
    }

    public double calculateEccentricAnomaly(double trueAnomaly, double eccentricity) {
        var rate = Math.sqrt((1 - eccentricity) / (1 + eccentricity));
        return Math.atan(rate * Math.tan(trueAnomaly / 2)) * 2;
    }

    public double calculateTrueAnomaly(double eccentricAnomaly, double eccentricity) {
        var rate = Math.sqrt((1 + eccentricity) / (1 - eccentricity));
        return Math.atan(rate * Math.tan(eccentricAnomaly / 2)) * 2;
    }

    public Point calculatePosition(Orbit orbit) {
        var eccentricAnomaly = calculateEccentricAnomaly(orbit.getTrueAnomaly(), orbit.getEccentricity());

        return calculatePosition(orbit, eccentricAnomaly);
    }

    public Point calculatePosition(Orbit orbit, double eccentricAnomaly) {
        var r = orbit.getSemiMajorAxis() * (1 - orbit.getEccentricity() * Math.cos(eccentricAnomaly));
        var x = r * Math.cos(orbit.getTrueAnomaly());
        var y = r * Math.sin(orbit.getTrueAnomaly());
        var newRadiusVector = Vector.of(x, y).rotate(orbit.getPerihelionArgument());

        return Point.of(newRadiusVector.getX(), newRadiusVector.getY());
    }
}
