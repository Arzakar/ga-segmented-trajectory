package org.klimashin.ga.segmented.trajectory.domain.model;

import org.klimashin.ga.segmented.trajectory.domain.model.common.Physics;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.TargetState;
import org.klimashin.ga.segmented.trajectory.domain.model.component.exception.NoOptimalSolutionException;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.CommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Simulator {

    private static final long maxDuration = Duration.ofDays(365).toSeconds();

    Environment env;

    CelestialBody centralBody;
    Map<CelestialBodyName, CelestialBody> celestialBodies;
    Spacecraft spacecraft;
    CommandProfile commandProfile;
    TargetState targetState;

    public Simulator(Environment env) {
        this.env = env;

        this.centralBody = env.getCentralBody();
        this.celestialBodies = env.getCelestialBodies();
        this.spacecraft = env.getSpacecraft();
        this.commandProfile = env.getCommandProfile();
        this.targetState = env.getTargetState();
    }

    public Environment execute(long deltaTime) {
        while (env.getCurrentTime() < maxDuration) {
            executeStep(env.getCurrentTime(), deltaTime);

            if (targetState.isAchieved()) {
                return env;
            }

            env.incrementTime(deltaTime);
        }

        throw new NoOptimalSolutionException(String.format("Длительность перелёта превысила %d секунд", maxDuration));
    }

    public List<Environment> detailedExecute(final int nodesCount) throws RuntimeException {
        var resultSet = new ArrayList<Environment>();

        var seconds = 0L;
        var deltaTime = 100L;

        var iteration = 0;
        var compressionRate = 1;

        while (seconds < maxDuration) {
            executeStep(seconds, deltaTime);

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

            seconds += deltaTime;
            iteration++;
        }

        resultSet.add(env.copy());
        return resultSet;
    }

    protected void executeStep(long currentTime, long deltaTime) {
        var isEnoughFuel = spacecraft.getFuelMass() > 0 && spacecraft.isEnoughFuel(deltaTime);

        var gravForceByCentral = Physics.gravitationForce(spacecraft, centralBody);
        var gravForceByBodies = Vector.zero();

        /** Пока убираем расчёт влияния Земли на КА
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
}
