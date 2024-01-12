package org.klimashin.ga.segmented.trajectory.domain.model.component;

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

    public double getFocalParameter() {
        return semiMajorAxis * (1 - eccentricity);
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
}
