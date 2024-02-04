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

@Slf4j
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
                var mostHighlyOrbit = calculateGravityAssistManeuver();

                return env.injectResultOrbit(mostHighlyOrbit);
            }

            env.incrementTime(deltaTime);
        }

        return env;
        //throw new NoOptimalSolutionException(String.format("Длительность перелёта превысила %d секунд", maxDuration), maxDuration, env);
    }

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

    protected Orbit calculateGravityAssistManeuver() {
        var earth = celestialBodies.get(CelestialBodyName.EARTH);

        var earthIncomingPos = earth.getPosition().copy();
        var earthIncomingSpd = earth.getSpeed().copy();

        var spacecraftIncomingPos = spacecraft.getPosition().asRadiusVector().subtract(earthIncomingPos.asRadiusVector());
        var spacecraftIncomingSpd = spacecraft.getSpeed().copy().subtract(earthIncomingSpd);

        var earthGravRad = 1_000_000_000;
        var earthGravParam = G * earth.getMass();
        var rHyp = 10_000_000d;
        var aHyp = earthGravParam / Math.pow(spacecraftIncomingSpd.getScalar(), 2);
        var bHyp = rHyp * Math.sqrt(1 + (2 * aHyp / rHyp));
        var eHyp = rHyp / aHyp + 1;
        var eHypDiff = Math.pow(eHyp, 2) - 1;
        var pHyp = aHyp * eHypDiff;

        var gamma = Math.atan(aHyp / bHyp);
        var alpha = Math.PI / 2 - gamma;
        var uOut = Math.acos((pHyp - earthGravRad) / (earthGravRad * eHyp));
        var sinUOut = Math.sin(uOut);
        var cosUOut = Math.cos(uOut);
        var A = ((eHyp * sinUOut + Math.sqrt(eHypDiff)) / (1 + eHyp * cosUOut)) + (1 / Math.sqrt(eHypDiff));
        var tSemiTrans = (Math.sqrt(pHyp / earthGravParam) / eHypDiff) * ((eHyp * pHyp * sinUOut / (1 + eHyp * cosUOut)) - (pHyp * Math.log(A) / Math.sqrt(eHyp - 1)));
        var tTrans = tSemiTrans * 2;

        if(tTrans <= 0) {
            return null;
        }

        var deltaTrueAnomaly = (tTrans / earth.getOrbit().getOrbitalPeriod()) * 2 * Math.PI;

        var earthOutgoingPos = earth.getOrbit().predictPosition(earth.getOrbit().getTrueAnomaly() + deltaTrueAnomaly);
        var earthOutgoingSpd = earth.getOrbit().predictSpeed(earth.getOrbit().getTrueAnomaly() + deltaTrueAnomaly);

        var radius = spacecraftIncomingPos.getScalar();
        var cosFi = spacecraftIncomingPos.getX() / radius;
        var sinFi = spacecraftIncomingPos.getY() / radius;
        var fi = Math.signum(sinFi) >= 0
                ? Math.acos(cosFi)
                : 2 * Math.PI - Math.acos(cosFi);

        var spacecraftRightOutgoingPos = ((Supplier<Point>) () -> {
            var fiOut = fi + 2 * uOut;
            var posAfterOut = Point.of(radius * Math.cos(fiOut), radius * Math.sin(fiOut)).asRadiusVector().add(earthOutgoingPos.asRadiusVector());
            return Point.of(posAfterOut.getX(), posAfterOut.getY());
        });
        var spacecraftRightOutgoingSpd = spacecraftIncomingSpd.copy().rotate(2 * gamma).add(earthOutgoingSpd);
        var rightResultOrbit = Orbit.buildByObservation(spacecraftRightOutgoingPos.get(), spacecraftRightOutgoingSpd, centralBody);

        var spacecraftLeftOutgoingPos = ((Supplier<Point>) () -> {
            var fiOut = fi - 2 * uOut;
            var posAfterOut = Point.of(radius * Math.cos(fiOut), radius * Math.sin(fiOut)).asRadiusVector().add(earthOutgoingPos.asRadiusVector());
            return Point.of(posAfterOut.getX(), posAfterOut.getY());
        });
        var spacecraftLeftOutgoingSpd = spacecraftIncomingSpd.copy().rotate(-2 * gamma).add(earthOutgoingSpd);
        var leftResultOrbit = Orbit.buildByObservation(spacecraftLeftOutgoingPos.get(), spacecraftLeftOutgoingSpd, centralBody);

        return rightResultOrbit.getApocenter() > leftResultOrbit.getApocenter()
                ? rightResultOrbit
                : leftResultOrbit;
    }
}
