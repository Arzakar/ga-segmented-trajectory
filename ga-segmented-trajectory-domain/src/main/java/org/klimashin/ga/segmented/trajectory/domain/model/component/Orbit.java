package org.klimashin.ga.segmented.trajectory.domain.model.component;

import static org.klimashin.ga.segmented.trajectory.domain.model.common.Physics.G;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Orbit {

    CelestialBody attractingBody;

    double apocenter;
    double pericenter;
    double semiMajorAxis;
    double eccentricity;
    double inclination;
    double longitudeAscNode;
    double perihelionArgument;

    @Setter
    double trueAnomaly;

    long zeroEpoch;

    public static Orbit buildByObservation(Point pos, Vector spd, CelestialBody attractingBody) {
        var rad = pos.asRadiusVector();

        var c = rad.vectorMultiply(spd);
        var mu = attractingBody.getMass() * G;
        var p = Math.pow(c, 2) / mu;
        var h = Math.pow(spd.getScalar(), 2) - (2 * mu) / rad.getScalar();
        var e = Math.sqrt(1 + (h * Math.pow(c, 2)) / Math.pow(mu, 2));

        var cosTrueAnomaly = (p - rad.getScalar()) / (rad.getScalar() * e);
        var sinTrueAnomaly = (spd.scalarMultiply(rad) * p) / (rad.getScalar() * c * e);
        var trueAnomaly = Math.signum(sinTrueAnomaly) >= 0
                ? Math.acos(cosTrueAnomaly)
                : 2 * Math.PI - Math.acos(cosTrueAnomaly);

        var cosU = rad.getX() / rad.getScalar();
        var sinU = rad.getY() / rad.getScalar();
        var u = Math.signum(sinU) >= 0
                ? Math.acos(cosU)
                : 2 * Math.PI - Math.acos(cosU);

        return Orbit.builder()
                .attractingBody(attractingBody)
                .apocenter(p / (1 - e))
                .pericenter(p / (1 + e))
                .semiMajorAxis(p / (1 - Math.pow(e, 2)))
                .eccentricity(e)
                .inclination(0)
                .longitudeAscNode(0)
                .perihelionArgument(u - trueAnomaly)
                .trueAnomaly(trueAnomaly)
                .zeroEpoch(0)
                .build();
    }

    public double getFocalParameter() {
        return semiMajorAxis * (1 - eccentricity);
    }

    public double getOrbitalPeriod() {
        return 2 * Math.PI * Math.sqrt(Math.pow(semiMajorAxis, 3) / (G * attractingBody.getMass()));
    }

    public Point getPosition() {
        var rate = Math.sqrt((1 - this.eccentricity) / (1 + this.eccentricity));
        var eccentricAnomaly = Math.atan(rate * Math.tan(this.trueAnomaly / 2)) * 2;

        var r = this.semiMajorAxis * (1 - this.eccentricity * Math.cos(eccentricAnomaly));
        var x = r * Math.cos(this.trueAnomaly);
        var y = r * Math.sin(this.trueAnomaly);
        var radiusVector = Vector.of(x, y).rotate(this.perihelionArgument);

        return Point.of(radiusVector.getX(), radiusVector.getY());
    }

    public Vector getSpeed() {
        var rate = Math.sqrt(G * this.attractingBody.getMass() / this.getFocalParameter());
        var radialSpeed = rate * this.eccentricity * Math.sin(this.trueAnomaly);
        var transversalSpeed = rate * (1 + this.eccentricity * Math.cos(this.trueAnomaly));

        return Vector.of(radialSpeed, transversalSpeed).rotate(this.trueAnomaly);
    }

    public Point predictPosition(double trueAnomaly) {
        var rate = Math.sqrt((1 - this.eccentricity) / (1 + this.eccentricity));
        var eccentricAnomaly = Math.atan(rate * Math.tan(trueAnomaly / 2)) * 2;

        var r = this.semiMajorAxis * (1 - this.eccentricity * Math.cos(eccentricAnomaly));
        var x = r * Math.cos(trueAnomaly);
        var y = r * Math.sin(trueAnomaly);
        var radiusVector = Vector.of(x, y).rotate(this.perihelionArgument);

        return Point.of(radiusVector.getX(), radiusVector.getY());
    }

    public Vector predictSpeed(double trueAnomaly) {
        var rate = Math.sqrt(G * this.attractingBody.getMass() / this.getFocalParameter());
        var radialSpeed = rate * this.eccentricity * Math.sin(trueAnomaly);
        var transversalSpeed = rate * (1 + this.eccentricity * Math.cos(trueAnomaly));

        return Vector.of(radialSpeed, transversalSpeed).rotate(trueAnomaly);
    }
}
