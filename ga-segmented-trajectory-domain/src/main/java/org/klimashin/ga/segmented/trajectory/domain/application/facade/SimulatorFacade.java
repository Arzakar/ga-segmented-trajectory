package org.klimashin.ga.segmented.trajectory.domain.application.facade;

import static org.klimashin.ga.segmented.trajectory.domain.model.common.Physics.G;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.CelestialBodyV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.EngineV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersCreationRequestV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersRandomCreationRequestV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SpacecraftV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.ApplicableSimResult;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.SimParametersEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.SimResultEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.CelestialBodyRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.SimParametersRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.SimResultRepository;
import org.klimashin.ga.segmented.trajectory.domain.model.Environment;
import org.klimashin.ga.segmented.trajectory.domain.model.Simulator;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.ProximityOfTwoObjects;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.TargetState;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.CommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.FvdCommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.common.DynamicCyclesIterator;
import org.klimashin.ga.segmented.trajectory.domain.util.common.PairRandom;
import org.klimashin.ga.segmented.trajectory.domain.util.component.LongSegment;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Pair;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimulatorFacade {

    CelestialBodyRepository celestialBodyRepository;
    SimParametersRepository simParametersRepository;
    SimResultRepository simResultRepository;

    TransactionTemplate transactionTemplate;

    public void createSimParametersAndStartCalculation(SimParametersCreationRequestV1Dto creationRequest) {
        var centralBody = celestialBodyRepository.findById(creationRequest.getCentralBodyName())
                .orElseThrow();
        var celestialBodies = creationRequest.getCelestialBodies().stream()
                .collect(Collectors.toMap(CelestialBodyV1Dto::getName, CelestialBodyV1Dto::getAnomaly));

        var spacecraft = creationRequest.getSpacecraft();
        var engine = creationRequest.getEngine();

        var simParametersTemplate = new SimParametersEntity()
                .setCentralBody(centralBody)
                .setCelestialBodiesByAnomalies(celestialBodies)
                .setSpacecraftPos(new Double[]{spacecraft.getPosX(), spacecraft.getPosY()})
                .setSpacecraftSpd(new Double[]{spacecraft.getSpdX(), spacecraft.getSpdY()})
                .setSpacecraftMass(spacecraft.getMass())
                .setSpacecraftFuelMass(spacecraft.getFuelMass())
                .setEngineFuelConsumption(engine.getFuelConsumption())
                .setEngineThrust(engine.getThrust())
                .setControlVariations(creationRequest.getControlVariations());

        var simParameters = simParametersRepository.saveAndFlush(simParametersTemplate);

        var rawComponents = new HashMap<Integer, Number[]>();
        for (int i = 0; i < simParameters.getControlVariations().size(); i++) {
            var duration = simParameters.getControlVariations().get(i)[0];
            var deviation = simParameters.getControlVariations().get(i)[1];

            rawComponents.put(i * 2, duration);
            rawComponents.put(i * 2 + 1, deviation);
        }

        new DynamicCyclesIterator(rawComponents).bulkExecute(12, numbers -> {
            final var initState = prepareInitState(simParameters);
            final var commandProfile = prepareCommandProfile(initState, numbers);
            final var targetState = prepareTargetState(initState);

            var resultState = Simulator.builder()
                    .env(initState)
                    .commandProfile(commandProfile)
                    .targetState(targetState)
                    .build()
                    .execute(100);

            var simResult = prepareSimResult(resultState, numbers)
                    .setSimParameters(simParameters)
                    .setIsMeetingEarth(targetState.isAchieved());

            if (targetState.isAchieved()) {
                var earth = resultState.getCelestialBodies().get(CelestialBodyName.EARTH);
                var orbitsAfterGa = calculateGravityAssistManeuverResult(resultState.getCentralBody(), earth, resultState.getSpacecraft());

                if (Optional.ofNullable(orbitsAfterGa).isPresent()) {
                    simResult.setApocenterAfterLeftGa(orbitsAfterGa.getLeft().getApocenter());
                    simResult.setApocenterAfterRightGa(orbitsAfterGa.getRight().getApocenter());
                }
            }

            transactionTemplate.executeWithoutResult(_ -> {
                var savedSimResult = simResultRepository.saveAndFlush(simResult);
                simParametersRepository.updateLastCalculate(simParameters.getId(), savedSimResult);
            });
        });

        simParametersRepository.updateLastCalculate(simParameters.getId(), null);
    }

    public void runRandomCalculations(SimParametersRandomCreationRequestV1Dto randomCreationRequest) {
        var random = new PairRandom();

        var spacecraft = randomCreationRequest.getSpacecraft();
        var engine = randomCreationRequest.getEngine();
        var controlMinVariations = randomCreationRequest.getControlMinVariations();
        var controlMaxVariations = randomCreationRequest.getControlMaxVariations();

        while (true) {
            var scMass = random.nextDouble(spacecraft.getMass());
            var controlVariations = controlMinVariations.keySet().stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            key -> {
                                var minDurations = controlMinVariations.get(key)[0];
                                var maxDurations = controlMaxVariations.get(key)[0];
                                var durations = new Number[minDurations.length];
                                for (int i = 0; i < minDurations.length; i++) {
                                    var minDuration = Integer.valueOf(minDurations[i].intValue());
                                    var maxDuration = Integer.valueOf(maxDurations[i].intValue());
                                    durations[i] = random.nextInt(Pair.of(minDuration, maxDuration));
                                }

                                var minDeviations = controlMinVariations.get(key)[1];
                                var maxDeviations = controlMaxVariations.get(key)[1];
                                var deviations = new Number[minDeviations.length];
                                for (int i = 0; i < minDeviations.length; i++) {
                                    var minDeviation = Double.valueOf(minDeviations[i].doubleValue());
                                    var maxDeviation = Double.valueOf(maxDeviations[i].doubleValue());
                                    deviations[i] = random.nextDouble(Pair.of(minDeviation, maxDeviation));
                                }

                                return new Number[][]{durations, deviations};
                            }
                    ));

            var creationRequest = SimParametersCreationRequestV1Dto.builder()
                    .centralBodyName(randomCreationRequest.getCentralBodyName())
                    .celestialBodies(randomCreationRequest.getCelestialBodies())
                    .spacecraft(SpacecraftV1Dto.builder()
                            .posX(random.nextDouble(spacecraft.getPosX()))
                            .posY(random.nextDouble(spacecraft.getPosY()))
                            .spdX(random.nextDouble(spacecraft.getSpdX()))
                            .spdY(random.nextDouble(spacecraft.getSpdY()))
                            .mass(scMass)
                            .fuelMass(scMass * random.nextDouble(spacecraft.getFuelMassPercent()))
                            .build())
                    .engine(EngineV1Dto.builder()
                            .fuelConsumption(random.nextDouble(engine.getFuelConsumption()))
                            .thrust(random.nextDouble(engine.getThrust()))
                            .build())
                    .controlVariations(controlVariations)
                    .build();

            this.createSimParametersAndStartCalculation(creationRequest);
        }
    }

    @Transactional
    public void downloadApplicableSimResults() {
        var simResultCompositeKey = new Function<ApplicableSimResult, List<Object>>() {
            public List<Object> apply(ApplicableSimResult applicableSimResult) {
                return Arrays.asList(
                        applicableSimResult.getSimParametersId(),
                        applicableSimResult.getInterval1(),
                        applicableSimResult.getDeviation1(),
                        applicableSimResult.getDeviation2(),
                        applicableSimResult.getRightResultApocenter(),
                        applicableSimResult.getLeftResultApocenter()
                );
            }
        };

        var results = simResultRepository.getApplicableSimResults()
                .map(result -> {
                    var simParameters = result.getSimParameters();
                    return ApplicableSimResult.builder()
                            .simParametersId(simParameters.getId())
                            .simResultId(result.getId())
                            .scStartPosX(simParameters.getSpacecraftPos()[0])
                            .scStartPosY(simParameters.getSpacecraftPos()[1])
                            .scStartSpdX(simParameters.getSpacecraftSpd()[0])
                            .scStartSpdY(simParameters.getSpacecraftSpd()[1])
                            .scStartMass(simParameters.getSpacecraftMass())
                            .scStartFuelMass(simParameters.getSpacecraftFuelMass())
                            .engConsumption(simParameters.getEngineFuelConsumption())
                            .engThrust(simParameters.getEngineThrust())
                            .scResPosX(result.getSpacecraftPos()[0])
                            .scResPosY(result.getSpacecraftPos()[1])
                            .scResSpdX(result.getSpacecraftSpd()[0])
                            .scResSpdY(result.getSpacecraftSpd()[1])
                            .scResMass(result.getSpacecraftMass())
                            .scResFuelMass(result.getSpacecraftFuelMass())
                            .interval1(result.getControlLaw().get(0)[0].doubleValue())
                            .deviation1(result.getControlLaw().get(0)[1].doubleValue())
                            .interval2(result.getControlLaw().get(1)[0].doubleValue())
                            .deviation2(result.getControlLaw().get(1)[1].doubleValue())
                            .e2eDuration(result.getEarthToEarthDuration())
                            .leftResultApocenter(result.getApocenterAfterLeftGa())
                            .rightResultApocenter(result.getApocenterAfterRightGa())
                            .build();
                })
                .collect(Collectors.groupingBy(simResultCompositeKey, Collectors.toList()));
                        //Collectors.minBy(Comparator.comparingDouble(ApplicableSimResult::getInterval2))));
    }

    private Environment prepareInitState(SimParametersEntity simParameters) {
        var centralBody = CelestialBody.builder()
                .name(simParameters.getCentralBody().getName())
                .mass(simParameters.getCentralBody().getMass())
                .orbit(null)
                .build();

        var celestialBodies = simParameters.getCelestialBodiesByAnomalies().entrySet().stream()
                .map(entry -> {
                    var celestialBodyEntity = celestialBodyRepository.findById(entry.getKey()).orElseThrow();
                    var orbitEntity = celestialBodyEntity.getOrbit();

                    var orbit = Orbit.builder()
                            .attractingBody(centralBody)
                            .apocenter(orbitEntity.getApocenter())
                            .pericenter(orbitEntity.getPericenter())
                            .semiMajorAxis(orbitEntity.getSemiMajorAxis())
                            .eccentricity(orbitEntity.getEccentricity())
                            .inclination(orbitEntity.getInclination())
                            .longitudeAscNode(orbitEntity.getLongitudeAscNode())
                            .perihelionArgument(orbitEntity.getPerihelionArgument())
                            .trueAnomaly(entry.getValue())
                            .zeroEpoch(0L)
                            .build();

                    return CelestialBody.builder()
                            .name(celestialBodyEntity.getName())
                            .mass(celestialBodyEntity.getMass())
                            .orbit(orbit)
                            .build();
                })
                .collect(Collectors.toMap(CelestialBody::getName, Function.identity()));

        var spacecraft = Spacecraft.builder()
                .position(Point.of(ArrayUtils.toPrimitive(simParameters.getSpacecraftPos())))
                .speed(Vector.of(ArrayUtils.toPrimitive(simParameters.getSpacecraftSpd())))
                .fuelConsumption(simParameters.getEngineFuelConsumption())
                .mass(simParameters.getSpacecraftMass())
                .fuelMass(simParameters.getSpacecraftFuelMass())
                .thrust(simParameters.getEngineThrust())
                .build();

        return Environment.builder()
                .centralBody(centralBody)
                .celestialBodies(celestialBodies)
                .spacecraft(spacecraft)
                .build();
    }

    private CommandProfile prepareCommandProfile(Environment environment, Number[] numbers) {
        var spacecraft = environment.getSpacecraft();
        var centralBody = environment.getCentralBody();

        var intervalsByDeviations = new TreeMap<LongSegment, Double>(LongSegment::compareByLeftTo);
        for (int idx = 0; idx < numbers.length; idx += 2) {
            var segment = idx == 0
                    ? LongSegment.of(0, Duration.ofDays(numbers[idx].longValue()).toSeconds())
                    : LongSegment.of(
                            Duration.ofDays(numbers[idx-2].longValue()).toSeconds(),
                            Duration.ofDays(numbers[idx-2].longValue() + numbers[idx].longValue()).toSeconds()
                    );
            var deviation = numbers[idx + 1].doubleValue();

            intervalsByDeviations.put(segment, Math.toRadians(deviation));
        }

        return FvdCommandProfile.builder()
                .startVectorObject(spacecraft)
                .endVectorObject(centralBody)
                .intervalsByDeviations(intervalsByDeviations)
                .build();
    }

    private TargetState prepareTargetState(Environment environment) {
        var spacecraft = environment.getSpacecraft();
        var celestialBodies = environment.getCelestialBodies();

        return ProximityOfTwoObjects.builder()
                .firstParticle(spacecraft)
                .secondParticle(celestialBodies.get(CelestialBodyName.EARTH))
                .requiredDistance(1_000_000_000)
                .build();
    }

    private SimResultEntity prepareSimResult(Environment resultState, Number[] numbers) {
        var celestialBodiesByAnomalies = resultState.getCelestialBodies().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getOrbit().getTrueAnomaly()));
        var spacecraft = resultState.getSpacecraft();

        var controlLaw = new TreeMap<Integer, Number[]>(Integer::compareTo);
        for (int idx = 0; idx < numbers.length; idx += 2) {
            var interval = numbers[idx];
            var deviation = numbers[idx + 1];

            controlLaw.put(idx / 2, new Number[]{interval, deviation});
        }

        return new SimResultEntity()
                .setCelestialBodiesByAnomalies(celestialBodiesByAnomalies)
                .setSpacecraftPos(ArrayUtils.toObject(spacecraft.getPosition().toArray()))
                .setSpacecraftSpd(ArrayUtils.toObject(spacecraft.getSpeed().toArray()))
                .setSpacecraftMass(spacecraft.getMass())
                .setSpacecraftFuelMass(spacecraft.getFuelMass())
                .setControlLaw(controlLaw)
                .setEarthToEarthDuration(resultState.getTime());
    }

    protected Pair<Orbit, Orbit> calculateGravityAssistManeuverResult(CelestialBody centralBody, CelestialBody celestialBody, Spacecraft spacecraft) {
        var celestialBodyIncomingPos = celestialBody.getPosition().copy();
        var celestialBodyIncomingSpd = celestialBody.getSpeed().copy();

        var spacecraftIncomingPos = spacecraft.getPosition().asRadiusVector().subtract(celestialBodyIncomingPos.asRadiusVector());
        var spacecraftIncomingSpd = spacecraft.getSpeed().copy().subtract(celestialBodyIncomingSpd);

        var gravRad = 1_000_000_000d;
        var gravParam = G * celestialBody.getMass();
        var rHyp = 10_000_000d;
        var aHyp = gravParam / Math.pow(spacecraftIncomingSpd.getScalar(), 2);
        var bHyp = rHyp * Math.sqrt(1 + (2 * aHyp / rHyp));
        var eHyp = rHyp / aHyp + 1;
        var eHypDiff = Math.pow(eHyp, 2) - 1;
        var pHyp = aHyp * eHypDiff;

        var gamma = Math.atan(aHyp / bHyp);
        var alpha = Math.PI / 2 - gamma;
        var uOut = Math.acos((pHyp - gravRad) / (gravRad * eHyp));
        var sinUOut = Math.sin(uOut);
        var cosUOut = Math.cos(uOut);
        var A = ((eHyp * sinUOut + Math.sqrt(eHypDiff)) / (1 + eHyp * cosUOut)) + (1 / Math.sqrt(eHypDiff));
        var tSemiTrans = (Math.sqrt(pHyp / gravParam) / eHypDiff) * ((eHyp * pHyp * sinUOut / (1 + eHyp * cosUOut)) - (pHyp * Math.log(A) / Math.sqrt(eHyp - 1)));
        var tTrans = tSemiTrans * 2;

        if(tTrans <= 0) {
            return null;
        }

        var deltaTrueAnomaly = (tTrans / celestialBody.getOrbit().getOrbitalPeriod()) * 2 * Math.PI;

        var celestialBodyOutgoingPos = celestialBody.getOrbit().predictPosition(celestialBody.getOrbit().getTrueAnomaly() + deltaTrueAnomaly);
        var celestialBodyOutgoingSpd = celestialBody.getOrbit().predictSpeed(celestialBody.getOrbit().getTrueAnomaly() + deltaTrueAnomaly);

        var radius = spacecraftIncomingPos.getScalar();
        var cosFi = spacecraftIncomingPos.getX() / radius;
        var sinFi = spacecraftIncomingPos.getY() / radius;
        var fi = Math.signum(sinFi) >= 0
                ? Math.acos(cosFi)
                : 2 * Math.PI - Math.acos(cosFi);

        var spacecraftRightOutgoingPos = ((Supplier<Point>) () -> {
            var fiOut = fi + 2 * uOut;
            var posAfterOut = Point.of(radius * Math.cos(fiOut), radius * Math.sin(fiOut)).asRadiusVector().add(celestialBodyOutgoingPos.asRadiusVector());
            return Point.of(posAfterOut.getX(), posAfterOut.getY());
        });
        var spacecraftRightOutgoingSpd = spacecraftIncomingSpd.copy().rotate(2 * gamma).add(celestialBodyOutgoingSpd);
        var rightResultOrbit = Orbit.buildByObservation(spacecraftRightOutgoingPos.get(), spacecraftRightOutgoingSpd, centralBody);

        var spacecraftLeftOutgoingPos = ((Supplier<Point>) () -> {
            var fiOut = fi - 2 * uOut;
            var posAfterOut = Point.of(radius * Math.cos(fiOut), radius * Math.sin(fiOut)).asRadiusVector().add(celestialBodyOutgoingPos.asRadiusVector());
            return Point.of(posAfterOut.getX(), posAfterOut.getY());
        });
        var spacecraftLeftOutgoingSpd = spacecraftIncomingSpd.copy().rotate(-2 * gamma).add(celestialBodyOutgoingSpd);
        var leftResultOrbit = Orbit.buildByObservation(spacecraftLeftOutgoingPos.get(), spacecraftLeftOutgoingSpd, centralBody);

        return new Pair<>(leftResultOrbit, rightResultOrbit);
    }
}
