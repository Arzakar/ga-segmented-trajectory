package org.klimashin.ga.segmented.trajectory.domain.model;

import org.klimashin.ga.segmented.trajectory.domain.model.common.Physics;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.TargetState;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.CommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Simulator {

    private static final long maxDuration = Duration.ofDays(365).toSeconds();

    Environment env;
    CommandProfile commandProfile;
    TargetState targetState;

    CelestialBody centralBody;
    Map<CelestialBodyName, CelestialBody> celestialBodies;
    Spacecraft spacecraft;

    public Simulator(Environment env, CommandProfile commandProfile, TargetState targetState) {
        this.env = env;
        this.commandProfile = commandProfile;
        this.targetState = targetState;

        this.centralBody = env.getCentralBody();
        this.celestialBodies = env.getCelestialBodies();
        this.spacecraft = env.getSpacecraft();
    }

    public Environment execute(long deltaTime) {
        while (env.getCurrentTime() < maxDuration) {
            executeStep(env.getCurrentTime(), deltaTime);

            if (targetState.isAchieved()) {
                return env;
            }

            env.incrementTime(deltaTime);
        }

        return env;
    }

    protected void executeStep(long currentTime, long deltaTime) {
        var isEnoughFuel = spacecraft.getFuelMass() > 0 && spacecraft.isEnoughFuel(deltaTime);

        var gravForceByCentral = Physics.gravitationForce(spacecraft, centralBody);
        var gravForceByBodies = Vector.zero();

        /* TODO: Пока убираем расчёт влияния Земли на КА
        var gravForceByBodies = celestialBodies.values().stream()
                .map(celestialBody -> Physics.gravitationForce(spacecraft, celestialBody))
                .reduce(Vector.zero(), Vector::add);
         */

        var gravForce = gravForceByCentral.add(gravForceByBodies);

        var thrustForce = isEnoughFuel
                ? commandProfile.getThrustForce(spacecraft.getThrust(), currentTime)
                : Vector.zero();

        var sumForce = gravForce.add(thrustForce);

        spacecraft.move(sumForce, deltaTime);
        celestialBodies.values().forEach(celestialBody -> celestialBody.move(deltaTime));

        if (isEnoughFuel) {
            spacecraft.reduceFuel(deltaTime);
        }
    }

    public static SimulatorBuilder builder() {
        return new SimulatorBuilder();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SimulatorBuilder {

        Environment env;
        CommandProfile commandProfile;
        TargetState targetState;

        public SimulatorBuilder env(Environment env) {
            this.env = env;
            return this;
        }

        public SimulatorBuilder commandProfile(CommandProfile commandProfile) {
            this.commandProfile = commandProfile;
            return this;
        }

        public SimulatorBuilder targetState(TargetState targetState) {
            this.targetState = targetState;
            return this;
        }

        public Simulator build() {
            return new Simulator(this.env, this.commandProfile, this.targetState);
        }
    }
}
