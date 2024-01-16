package org.klimashin.ga.segmented.trajectory.domain.application.facade;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.ResultEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.mapper.InitialMapper;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.CelestialBodyRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.InitialRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.ResultRepository;
import org.klimashin.ga.segmented.trajectory.domain.model.Environment;
import org.klimashin.ga.segmented.trajectory.domain.model.Simulator;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.ProximityOfTwoObjects;
import org.klimashin.ga.segmented.trajectory.domain.model.component.exception.NoOptimalSolutionException;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.FvdCommandProfile;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InitialFacade {

    CelestialBodyRepository celestialBodyRepository;
    InitialRepository initialRepository;
    InitialMapper initialMapper;
    ResultRepository resultRepository;

    PlatformTransactionManager transactionManager;

    public void startCalculation(InitialCreationRequestDto creationRequestDto) {
        var transactionTemplate = new TransactionTemplate(transactionManager);

        var centralBodyEntity = celestialBodyRepository.findById(creationRequestDto.getCentralBodyName()).orElseThrow();
        var celestialBodyEntity = celestialBodyRepository.findById(creationRequestDto.getCelestialBodyName()).orElseThrow();
        var celestialBodyOrbitEntity = celestialBodyEntity.getOrbit();
        var initialEntity = initialMapper.creationRequestDtoToEntity(creationRequestDto)
                .setCentralBody(centralBodyEntity)
                .setCelestialBody(celestialBodyEntity);

        var savedInitialEntity = initialRepository.saveAndFlush(initialEntity);

        var intervalLeft = savedInitialEntity.getIntervalLeftBound();
        var intervalRight = savedInitialEntity.getIntervalRightBound();
        var intervalStep = savedInitialEntity.getIntervalStep();

        var deviationLeft = savedInitialEntity.getDeviationLeftBound();
        var deviationRight = savedInitialEntity.getDeviationRightBound();
        var deviationStep = savedInitialEntity.getDeviationStep();

        for (long interval1 = intervalLeft; interval1 <= intervalRight; interval1 += intervalStep) {
            for (double deviation1 = deviationLeft; deviation1 <= deviationRight; deviation1 += deviationStep) {
                for (long interval2 = intervalLeft; interval2 <= intervalRight; interval2 += intervalStep) {
                    for (double deviation2 = deviationLeft; deviation2 <= deviationRight; deviation2 += deviationStep) {
                        log.debug("Calculating {}, {}, {}, {}", interval1, deviation1, interval2, deviation2);

                        var centralBody = CelestialBody.builder()
                                .name(centralBodyEntity.getName())
                                .mass(centralBodyEntity.getMass())
                                .orbit(null)
                                .build();

                        var celestialBody = CelestialBody.builder()
                                .name(celestialBodyEntity.getName())
                                .mass(celestialBodyEntity.getMass())
                                .orbit(Orbit.builder()
                                        .attractingBody(centralBody)
                                        .apocenter(celestialBodyOrbitEntity.getApocenter())
                                        .pericenter(celestialBodyOrbitEntity.getPericenter())
                                        .semiMajorAxis(celestialBodyOrbitEntity.getSemiMajorAxis())
                                        .eccentricity(celestialBodyOrbitEntity.getEccentricity())
                                        .inclination(celestialBodyOrbitEntity.getInclination())
                                        .longitudeAscNode(celestialBodyOrbitEntity.getLongitudeAscNode())
                                        .perihelionArgument(celestialBodyOrbitEntity.getPerihelionArgument())
                                        .trueAnomaly(savedInitialEntity.getCelestialBodyAnomaly())
                                        .zeroEpoch(0L)
                                        .build())
                                .build();

                        var spacecraft = Spacecraft.builder()
                                .position(Point.of(savedInitialEntity.getSpacecraftPosX(), savedInitialEntity.getSpacecraftPosY()))
                                .speed(Vector.of(savedInitialEntity.getSpacecraftSpdX(), savedInitialEntity.getSpacecraftSpdY()))
                                .fuelConsumption(savedInitialEntity.getFuelConsumption())
                                .mass(savedInitialEntity.getMass())
                                .fuelMass(savedInitialEntity.getFuelMass())
                                .thrust(savedInitialEntity.getThrust())
                                .build();

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
                                .requiredDistance(savedInitialEntity.getRequiredDistance())
                                .build();

                        var initialEnvironment = Environment.builder()
                                .centralBody(centralBody)
                                .celestialBodies(Map.of(celestialBody.getName(), celestialBody))
                                .spacecraft(spacecraft)
                                .commandProfile(commandProfile)
                                .targetState(targetState)
                                .build();

                        try {
                            var result = new Simulator(initialEnvironment).execute(100);

                            var resultCelestialBody = result.getCelestialBodies().get(savedInitialEntity.getCelestialBody().getName());
                            var resultSpacecraft = result.getSpacecraft();
                            var resultEntity = new ResultEntity()
                                    .setInitialEntity(savedInitialEntity)
                                    .setCelestialBodyAnomaly(resultCelestialBody.getOrbit().getTrueAnomaly())
                                    .setSpacecraftPosX(resultSpacecraft.getPosition().getX())
                                    .setSpacecraftPosY(resultSpacecraft.getPosition().getY())
                                    .setSpacecraftSpdX(resultSpacecraft.getSpeed().getX())
                                    .setSpacecraftSpdY(resultSpacecraft.getSpeed().getY())
                                    .setMass(resultSpacecraft.getMass())
                                    .setFuelMass(resultSpacecraft.getFuelMass())
                                    .setFirstInterval(interval1)
                                    .setFirstDeviation(deviation1)
                                    .setSecondInterval(interval2)
                                    .setSecondDeviation(deviation2)
                                    .setIsComplete(result.getTargetState().isAchieved())
                                    .setDuration(result.getTime());

                            transactionTemplate.executeWithoutResult(status -> {
                                var savedResultEntity = resultRepository.saveAndFlush(resultEntity);
                                initialRepository.updateLastCalculate(savedInitialEntity.getId(), savedResultEntity);
                            });

                        } catch (NoOptimalSolutionException exception) {
                            var result = exception.getEnvironment();

                            var resultCelestialBody = result.getCelestialBodies().get(savedInitialEntity.getCelestialBody().getName());
                            var resultSpacecraft = result.getSpacecraft();
                            var resultEntity = new ResultEntity()
                                    .setInitialEntity(savedInitialEntity)
                                    .setCelestialBodyAnomaly(resultCelestialBody.getOrbit().getTrueAnomaly())
                                    .setSpacecraftPosX(resultSpacecraft.getPosition().getX())
                                    .setSpacecraftPosY(resultSpacecraft.getPosition().getY())
                                    .setSpacecraftSpdX(resultSpacecraft.getSpeed().getX())
                                    .setSpacecraftSpdY(resultSpacecraft.getSpeed().getY())
                                    .setMass(resultSpacecraft.getMass())
                                    .setFuelMass(resultSpacecraft.getFuelMass())
                                    .setFirstInterval(interval1)
                                    .setFirstDeviation(deviation1)
                                    .setSecondInterval(interval2)
                                    .setSecondDeviation(deviation2)
                                    .setIsComplete(result.getTargetState().isAchieved())
                                    .setDuration(result.getTime());

                            transactionTemplate.executeWithoutResult(status -> {
                                var savedResultEntity = resultRepository.saveAndFlush(resultEntity);
                                initialRepository.updateLastCalculate(savedInitialEntity.getId(), savedResultEntity);
                            });
                        }
                    }
                }
            }
        }

        initialRepository.updateLastCalculate(savedInitialEntity.getId(), null);
    }
}
