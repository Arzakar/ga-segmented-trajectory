package org.klimashin.ga.segmented.trajectory.domain.model;

import static org.klimashin.ga.segmented.trajectory.domain.model.common.Physics.G;

import org.klimashin.ga.segmented.trajectory.domain.model.common.Physics;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.TargetState;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.CommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    /*
    public List<Environment> detailedExecute(final int nodesCount) throws RuntimeException {
        var resultSet = new ArrayList<Environment>();

        var deltaTime = 100L;

        var iteration = 0;
        var compressionRate = 1;

        while (env.getCurrentTime() < maxDuration) {
            executeStep(env.getCurrentTime(), deltaTime);

            if (iteration % compressionRate == 0) {
                resultSet.add(env.copy());

                if (resultSet.size() > nodesCount) {
                    compressionRate *= 2;

                    var compressedResultSet = IntStream.range(0, resultSet.size())
                            .filter(value -> value % 2 == 0)
                            .mapToObj(resultSet::get)
                            .collect(Collectors.toCollection(ArrayList::new));

                    var lastElement = resultSet.get(resultSet.size() - 1);
                    if (!compressedResultSet.contains(lastElement)) {
                        compressedResultSet.add(lastElement);
                    }

                    resultSet = compressedResultSet;
                }
            }

            if (env.getTargetState().isAchieved()) {
                resultSet.add(env.copy());
                return resultSet;
            }

            env.incrementTime(deltaTime);
            iteration++;
        }

        resultSet.add(env.copy());
        return resultSet;
    }
    */

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
