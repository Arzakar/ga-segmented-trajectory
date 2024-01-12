package org.klimashin.ga.segmented.trajectory.domain.model.component;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Spacecraft implements MassParticle {

    final Point position;
    final Vector speed;

    final double fuelConsumption;

    double mass;
    double fuelMass;

    double thrust;

    public Spacecraft move(Vector appliedForce, double deltaTime) {
        var acceleration = appliedForce.copy().divide(mass);

        speed.add(acceleration.copy().multiply(deltaTime));
        position.move(speed.copy().multiply(deltaTime));

        return this;
    }

    public boolean isEnoughFuel(double deltaTime) {
        var requiredFuelMass = fuelConsumption * deltaTime;
        return fuelMass >= requiredFuelMass;
    }

    public Spacecraft reduceFuel(double deltaTime) {
        var deltaMass = fuelConsumption * deltaTime;
        fuelMass -= deltaMass;
        mass -= deltaMass;

        return this;
    }
}
