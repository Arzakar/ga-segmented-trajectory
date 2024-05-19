package org.klimashin.ga.segmented.trajectory.domain.application.facade;

import static java.lang.System.gc;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.InitialEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.ResultEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.mapper.InitialMapper;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.CelestialBodyRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.InitialRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.ResultRepository;
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

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimulatorFacade {

    CelestialBodyRepository celestialBodyRepository;
    InitialRepository initialRepository;
    InitialMapper initialMapper;
    ResultRepository resultRepository;

    TransactionTemplate transactionTemplate;

    public void startRandomCalculations() {
        while (true) {
            var spacecraftSpdY = 29700d + Math.random() * 300d;
            var mass = 300d + Math.random() * 2700d;
            var fuelMass = mass * (0.1 + Math.random() * 0.7);
            var oneThrust = 0.150 + Math.random() * 250;
            var consumption = 0.00001 + Math.random() * 0.00002;
            var engineCount = (int) (Math.random() * 3) + 1;

            var intervalStep = 10L + (long) ((Math.random() * 10) - 5);
            var deviationStep = 30d + ((Math.random() * 30) - 15);

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
                    .intervalLeftBound(5L)
                    .intervalRightBound(180L)
                    .intervalStep(intervalStep)
                    .deviationLeftBound(-180d)
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
