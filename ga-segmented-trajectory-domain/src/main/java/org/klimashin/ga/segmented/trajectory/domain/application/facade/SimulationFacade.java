package org.klimashin.ga.segmented.trajectory.domain.application.facade;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.ApplicableSimResult;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.InitialEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.ResultEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.SimParametersEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.SimResultEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.mapper.InitialMapper;
import org.klimashin.ga.segmented.trajectory.domain.application.mapper.SimParametersMapper;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.CelestialBodyRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.InitialRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.ResultRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.SimParametersRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.SimResultRepository;
import org.klimashin.ga.segmented.trajectory.domain.model.Environment;
import org.klimashin.ga.segmented.trajectory.domain.model.Simulator;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.ProximityOfTwoObjects;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.FvdCommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.common.DynamicCyclesIterator;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimulationFacade {

    CelestialBodyRepository celestialBodyRepository;
    InitialRepository initialRepository;
    InitialMapper initialMapper;
    ResultRepository resultRepository;

    SimParametersMapper simParametersMapper;
    SimParametersRepository simParametersRepository;
    SimResultRepository simResultRepository;

    TransactionTemplate transactionTemplate;

    public void startRandomCalculations() {
        while (true) {
            var spacecraftSpdY = 29700d + Math.random() * 300d;
            var mass = 300d + Math.random() * 1700d;
            var fuelMass = mass * (0.2 + Math.random() * 0.4);
            var oneThrust = 150 + Math.random() * 250;
            var consumption = 0.00001 + Math.random() * 0.00002;
            var engineCount = (int) (Math.random() * 3) + 1;

            var intervalStep = 10L + (long) ((Math.random() * 4) - 2);
            var deviationStep = 30d + ((Math.random() * 10) - 5);

            var request = InitialCreationRequestDto.builder()
                    .centralBodyName(CelestialBodyName.SOLAR)
                    .celestialBodyName(CelestialBodyName.EARTH)
                    .celestialBodyAnomaly(0d)
                    .spacecraftPosX(146100393440d)
                    .spacecraftPosY(0d)
                    .spacecraftSpdX(0d)
                    .spacecraftSpdY(spacecraftSpdY)
                    .fuelConsumption(consumption * engineCount)
                    .mass(mass)
                    .fuelMass(fuelMass)
                    .thrust(oneThrust * engineCount)
                    .intervalLeftBound(10L)
                    .intervalRightBound(180L)
                    .intervalStep(intervalStep)
                    .deviationLeftBound(-150d)
                    .deviationRightBound(180d)
                    .deviationStep(deviationStep)
                    .requiredDistance(1000000000d)
                    .build();

            startCalculation(request);
        }
    }

    public void startCalculation(InitialCreationRequestDto creationRequestDto) {
        var initial = initialRepository.saveAndFlush(initialMapper.creationRequestDtoToEntity(creationRequestDto)
                .setCentralBody(celestialBodyRepository.findById(creationRequestDto.getCentralBodyName()).orElseThrow())
                .setCelestialBody(celestialBodyRepository.findById(creationRequestDto.getCelestialBodyName()).orElseThrow()));

        var intervalArray = buildIntervalArray(initial);
        var deviationArray = buildDeviationArray(initial);

        var startTime = LocalDateTime.now();
        var rawComponents = Map.of(0, intervalArray, 1, deviationArray, 2, intervalArray, 3, deviationArray);
        new DynamicCyclesIterator(rawComponents).bulkExecute(numbers -> calculatingFunction(initial, numbers));
        var endTime = LocalDateTime.now();
        log.info("Времени затрачено: {} c", Duration.between(startTime, endTime).toSeconds());

        initialRepository.updateLastCalculate(initial.getId(), null);
    }

    public void resumeCalculation(UUID initialId) {
        var initial = initialRepository.findById(initialId).orElseThrow();
        var lastCalculated = initial.getLastCalculate();

        var intervalArray = buildIntervalArray(initial);
        var deviationArray = buildDeviationArray(initial);
        var rawComponents = Map.of(0, intervalArray, 1, deviationArray, 2, intervalArray, 3, deviationArray);

        var cellRef0 = IntStream.range(0, intervalArray.length).filter(i -> intervalArray[i].equals(lastCalculated.getFirstInterval())).findFirst().orElseThrow();
        var cellRef1 = IntStream.range(0, deviationArray.length).filter(i -> deviationArray[i].equals(lastCalculated.getFirstDeviation())).findFirst().orElseThrow();
        var cellRef2 = IntStream.range(0, intervalArray.length).filter(i -> intervalArray[i].equals(lastCalculated.getSecondInterval())).findFirst().orElseThrow();
        var cellRef3 = IntStream.range(0, deviationArray.length).filter(i -> deviationArray[i].equals(lastCalculated.getSecondDeviation())).findFirst().orElseThrow();

        var cellRefs = Map.of(0, cellRef0, 1, cellRef1, 2, cellRef2, 3, cellRef3);

        new DynamicCyclesIterator(rawComponents, cellRefs).bulkExecute(numbers -> calculatingFunction(initial, numbers));

        initialRepository.updateLastCalculate(initial.getId(), null);
    }

    protected void calculatingFunction(InitialEntity initial, Number[] numbers) {
        log.debug("Calculating {}", Arrays.toString(numbers));

        var initialEnvironment = buildInitialEnvironment(initial, numbers);

        var result = new Simulator(initialEnvironment).execute(100);
        var resultEntity = buildResultEntity(result, initial, numbers);

        transactionTemplate.executeWithoutResult(status -> {
            var savedResultEntity = resultRepository.saveAndFlush(resultEntity);
            initialRepository.updateLastCalculate(initial.getId(), savedResultEntity);
        });

    }


    @Async
    public void createSimParametersAndStartCalculation(SimParametersCreationRequestDto creationRequest) {
        var simParameters = simParametersRepository.saveAndFlush(simParametersMapper.creationRequestDtoToEntity(creationRequest)
                .setCentralBody(celestialBodyRepository.findById(creationRequest.getCentralBody()).orElseThrow()));

        var rawComponents = new HashMap<Integer, Number[]>();
        for (int i = 0; i < simParameters.getControlVariations().size(); i++) {
            var duration = simParameters.getControlVariations().get(i)[0];
            var deviation = simParameters.getControlVariations().get(i)[1];

            rawComponents.put(i * 2, duration);
            rawComponents.put(i * 2 + 1, deviation);
        }

        new DynamicCyclesIterator(rawComponents).bulkExecute(numbers -> {
            var environment = buildEnvironment(simParameters, numbers);

            var resultState = new Simulator(environment).execute(100);
            var simResult = buildSimResult(resultState, simParameters, numbers);

            transactionTemplate.executeWithoutResult(status -> {
                var savedSimResult = simResultRepository.saveAndFlush(simResult);
                simParametersRepository.updateLastCalculate(simParameters.getId(), savedSimResult);
            });
        });

        simParametersRepository.updateLastCalculate(simParameters.getId(), null);
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

    private Environment buildEnvironment(SimParametersEntity simParameters, Number[] numbers) {
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

        var endIntervalsByDeviations = new TreeMap<Long, Double>(Long::compareTo);
        for (int idx = 0; idx < numbers.length; idx += 2) {
            var interval = numbers[idx].longValue() + endIntervalsByDeviations.keySet().stream().reduce(0L, Long::sum);
            var deviation = numbers[idx + 1].doubleValue();

            endIntervalsByDeviations.put(Duration.ofDays(interval).toSeconds(), Math.toRadians(deviation));
        }

        var commandProfile = FvdCommandProfile.builder()
                .startVectorObject(spacecraft)
                .endVectorObject(centralBody)
                .endIntervalsByDeviations(endIntervalsByDeviations)
                .build();

        var targetState = ProximityOfTwoObjects.builder()
                .firstParticle(spacecraft)
                .secondParticle(celestialBodies.get(CelestialBodyName.EARTH))
                .requiredDistance(1_000_000_000)
                .build();

        return Environment.builder()
                .centralBody(centralBody)
                .celestialBodies(celestialBodies)
                .spacecraft(spacecraft)
                .commandProfile(commandProfile)
                .targetState(targetState)
                .build();
    }

    private SimResultEntity buildSimResult(Environment resultState, SimParametersEntity simParameters, Number[] numbers) {
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
                .setSimParameters(simParameters)
                .setCelestialBodiesByAnomalies(celestialBodiesByAnomalies)
                .setSpacecraftPos(ArrayUtils.toObject(spacecraft.getPosition().toArray()))
                .setSpacecraftSpd(ArrayUtils.toObject(spacecraft.getSpeed().toArray()))
                .setSpacecraftMass(spacecraft.getMass())
                .setSpacecraftFuelMass(spacecraft.getFuelMass())
                .setControlLaw(controlLaw)
                .setIsMeetingEarth(resultState.getTargetState().isAchieved())
                .setEarthToEarthDuration(resultState.getTime())
                .setApocenterAfterLeftGa(Optional.ofNullable(resultState.getResultLeftOrbit()).map(Orbit::getApocenter).orElse(null))
                .setApocenterAfterRightGa(Optional.ofNullable(resultState.getResultRightOrbit()).map(Orbit::getApocenter).orElse(null));
    }


    protected Environment buildInitialEnvironment(InitialEntity initial, Number[] numbers) {
        var centralBody = buildCentralBody(initial);
        var celestialBody = buildCelestialBody(initial, centralBody);
        var spacecraft = buildSpacecraft(initial);

        var interval1 = (long) numbers[0];
        var deviation1 = (double) numbers[1];
        var interval2 = (long) numbers[2];
        var deviation2 = (double) numbers[3];
        var endIntervalsByDeviations = new TreeMap<Long, Double>(Long::compareTo);
        endIntervalsByDeviations.put(Duration.ofDays(interval1).toSeconds(), Math.toRadians(deviation1));
        endIntervalsByDeviations.put(Duration.ofDays(interval1 + interval2).toSeconds(), Math.toRadians(deviation2));

        var commandProfile = FvdCommandProfile.builder()
                .startVectorObject(spacecraft)
                .endVectorObject(centralBody)
                .endIntervalsByDeviations(endIntervalsByDeviations)
                .build();

        var targetState = ProximityOfTwoObjects.builder()
                .firstParticle(spacecraft)
                .secondParticle(celestialBody)
                .requiredDistance(initial.getRequiredDistance())
                .build();

        return Environment.builder()
                .centralBody(centralBody)
                .celestialBodies(Map.of(celestialBody.getName(), celestialBody))
                .spacecraft(spacecraft)
                .commandProfile(commandProfile)
                .targetState(targetState)
                .build();
    }

    protected ResultEntity buildResultEntity(Environment result, InitialEntity initial, Number[] numbers) {
        var celestialBody = result.getCelestialBodies().get(initial.getCelestialBody().getName());
        var spacecraft = result.getSpacecraft();

        var interval1 = (long) numbers[0];
        var deviation1 = (double) numbers[1];
        var interval2 = (long) numbers[2];
        var deviation2 = (double) numbers[3];

        return new ResultEntity()
                .setInitialEntity(initial)
                .setCelestialBodyAnomaly(celestialBody.getOrbit().getTrueAnomaly())
                .setSpacecraftPosX(spacecraft.getPosition().getX())
                .setSpacecraftPosY(spacecraft.getPosition().getY())
                .setSpacecraftSpdX(spacecraft.getSpeed().getX())
                .setSpacecraftSpdY(spacecraft.getSpeed().getY())
                .setMass(spacecraft.getMass())
                .setFuelMass(spacecraft.getFuelMass())
                .setFirstInterval(interval1)
                .setFirstDeviation(deviation1)
                .setSecondInterval(interval2)
                .setSecondDeviation(deviation2)
                .setIsComplete(result.getTargetState().isAchieved())
                .setDuration(result.getTime())
                .setResultApocenter(Optional.ofNullable(result.getResultOrbit()).map(Orbit::getApocenter).orElse(null));
    }

    protected CelestialBody buildCentralBody(InitialEntity initial) {
        return CelestialBody.builder()
                .name(initial.getCentralBody().getName())
                .mass(initial.getCentralBody().getMass())
                .orbit(null)
                .build();
    }

    protected CelestialBody buildCelestialBody(InitialEntity initial,
                                               CelestialBody centralBody) {
        var orbit = Orbit.builder()
                .attractingBody(centralBody)
                .apocenter(initial.getCelestialBody().getOrbit().getApocenter())
                .pericenter(initial.getCelestialBody().getOrbit().getPericenter())
                .semiMajorAxis(initial.getCelestialBody().getOrbit().getSemiMajorAxis())
                .eccentricity(initial.getCelestialBody().getOrbit().getEccentricity())
                .inclination(initial.getCelestialBody().getOrbit().getInclination())
                .longitudeAscNode(initial.getCelestialBody().getOrbit().getLongitudeAscNode())
                .perihelionArgument(initial.getCelestialBody().getOrbit().getPerihelionArgument())
                .trueAnomaly(initial.getCelestialBodyAnomaly())
                .zeroEpoch(0L)
                .build();

        return CelestialBody.builder()
                .name(initial.getCelestialBody().getName())
                .mass(initial.getCelestialBody().getMass())
                .orbit(orbit)
                .build();
    }

    protected Spacecraft buildSpacecraft(InitialEntity initial) {
        var position = Point.of(initial.getSpacecraftPosX(), initial.getSpacecraftPosY());
        var speed = Vector.of(initial.getSpacecraftSpdX(), initial.getSpacecraftSpdY());

        return Spacecraft.builder()
                .position(position)
                .speed(speed)
                .fuelConsumption(initial.getFuelConsumption())
                .mass(initial.getMass())
                .fuelMass(initial.getFuelMass())
                .thrust(initial.getThrust())
                .build();
    }

    protected Number[] buildIntervalArray(InitialEntity initial) {
        return LongStream.iterate(
                        initial.getIntervalLeftBound(),
                        value -> value <= initial.getIntervalRightBound(),
                        value -> value + initial.getIntervalStep()
                ).mapToObj(value -> (Number) value)
                .toArray(Number[]::new);
    }

    protected Number[] buildDeviationArray(InitialEntity initial) {
        return DoubleStream.iterate(
                        initial.getDeviationLeftBound(),
                        value -> value <= initial.getDeviationRightBound(),
                        value -> value + initial.getDeviationStep()
                ).mapToObj(value -> (Number) value)
                .toArray(Number[]::new);
    }
}
