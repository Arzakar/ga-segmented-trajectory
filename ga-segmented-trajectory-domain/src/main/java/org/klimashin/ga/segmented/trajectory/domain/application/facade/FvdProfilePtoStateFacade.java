package org.klimashin.ga.segmented.trajectory.domain.application.facade;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSnapshot;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateInitialData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateResultData;
import org.klimashin.ga.segmented.trajectory.domain.application.mapper.EnvironmentMapper;
import org.klimashin.ga.segmented.trajectory.domain.application.service.FvdProfilePtoStateService;
import org.klimashin.ga.segmented.trajectory.domain.model.Environment;
import org.klimashin.ga.segmented.trajectory.domain.model.Simulator;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.condition.ProximityOfTwoObjects;
import org.klimashin.ga.segmented.trajectory.domain.model.component.exception.NoOptimalSolutionException;
import org.klimashin.ga.segmented.trajectory.domain.model.component.profile.FvdCommandProfile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FvdProfilePtoStateFacade {

    EnvironmentMapper environmentMapper;
    FvdProfilePtoStateService fvdProfilePtoStateService;

    public void runCalculations(FvdProfilePtoStateInitialData initialData) {
        var savedInitialData = fvdProfilePtoStateService.saveInitial(initialData);

        var commandProfileSetup = initialData.getCommandProfileSetup();

        var durationLeftBound = commandProfileSetup.getIntervalDurationLeftBound();
        var durationRightBound = commandProfileSetup.getIntervalDurationRightBound();
        var durationStep = commandProfileSetup.getIntervalDurationStep();

        var deviationLeftBound = commandProfileSetup.getDeviationLeftBound();
        var deviationRightBound = commandProfileSetup.getDeviationRightBound();
        var deviationStep = commandProfileSetup.getDeviationStep();

        for (long firstDuration = durationLeftBound; firstDuration <= durationRightBound; firstDuration += durationStep) {
            for (double firstDeviation = deviationLeftBound; firstDeviation <= deviationRightBound; firstDeviation += deviationStep) {
                for (long secondDuration = durationLeftBound; secondDuration <= durationRightBound; secondDuration += durationStep) {
                    for (double secondDeviation = deviationLeftBound; secondDeviation <= deviationRightBound; secondDeviation += deviationStep) {
                        var centralBody = environmentMapper.dataToModel(initialData.getCentralBody());
                        var celestialBodies = environmentMapper.dataToModel(initialData.getCelestialBodies());
                        var spacecraft = environmentMapper.dataToModel(initialData.getSpacecraft());

                        var endIntervalsByDeviations = new TreeMap<Long, Double>(Long::compareTo);
                        endIntervalsByDeviations.put(firstDuration, firstDeviation);
                        endIntervalsByDeviations.put(firstDuration + secondDuration, secondDeviation);

                        var commandProfile = FvdCommandProfile.builder()
                                .startVectorObject(spacecraft)
                                .endVectorObject(celestialBodies.get(CelestialBodyName.valueOf(commandProfileSetup.getTargetObjectName().name())))
                                .endIntervalsByDeviations(endIntervalsByDeviations)
                                .build();

                        var targetState = ProximityOfTwoObjects.builder()
                                .firstParticle(spacecraft)
                                .secondParticle(celestialBodies.get(CelestialBodyName.valueOf(initialData.getTargetStateSetup().getTargetObjectName().name())))
                                .requiredDistance(initialData.getTargetStateSetup().getRequiredDistance())
                                .build();

                        var initialEnvironment = Environment.builder()
                                .centralBody(centralBody)
                                .celestialBodies(celestialBodies)
                                .spacecraft(spacecraft)
                                .commandProfile(commandProfile)
                                .targetState(targetState)
                                .build();

                        Environment result;

                        try {
                            result = new Simulator(initialEnvironment).execute(100);

                        } catch (NoOptimalSolutionException exception) {
                            result = initialEnvironment;

                        }

                        var calculatedCommandProfile = FvdCommandProfileSnapshot.builder()
                                .intervalsByDurations(Map.of(0, firstDuration, 1, secondDuration))
                                .intervalsByDeviations(Map.of(0, firstDeviation, 1, secondDeviation))
                                .build();
                        var resultData = FvdProfilePtoStateResultData.builder()
                                .initialId(savedInitialData.getId())
                                .celestialBodies(environmentMapper.modelToData(result.getCelestialBodies()))
                                .spacecraft(environmentMapper.modelToData(result.getSpacecraft()))
                                .calculatedCommandProfile(calculatedCommandProfile)
                                .targetStateIsAchieved(result.getTargetState().isAchieved())
                                .duration(result.getCurrentTime())
                                .build();

                        fvdProfilePtoStateService.saveResult(resultData);
                        fvdProfilePtoStateService.updateLastCalculatedCommandProfile(initialData.getId(), calculatedCommandProfile);
                    }
                }
            }
        }
    }
}
