package org.klimashin.ga.segmented.trajectory.domain.model.common;

import org.klimashin.ga.segmented.trajectory.domain.model.component.MassParticle;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Points;
import org.klimashin.ga.segmented.trajectory.domain.util.common.Vectors;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Physics {

    public static final double G = 6.67430 * Math.pow(10, -11);

    public static double gravitationParameter(double mass) {
        return G * mass;
    }

    public static double gravitationParameter(double firstMass, double secondMass) {
        return G * (firstMass + secondMass);
    }

    public static double gravitationForceMagnitude(MassParticle firstParticle, MassParticle secondParticle) {
        var distance = Points.distanceBetween(firstParticle.getPosition(), secondParticle.getPosition());
        return G * ((firstParticle.getMass() * secondParticle.getMass()) / Math.pow(distance, 2));
    }

    public static Vector gravitationForce(MassParticle gravitatingBody, MassParticle attractiveBody) {
        return Vectors.between(gravitatingBody.getPosition(), attractiveBody.getPosition())
                .toUnit()
                .multiply(Physics.gravitationForceMagnitude(gravitatingBody, attractiveBody));
    }
}
