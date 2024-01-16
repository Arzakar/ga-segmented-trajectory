package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class FvdCommandProfileSetup {

    CelestialBodyName targetObjectName;

    Integer intervalsCount;

    Long intervalDurationLeftBound;
    Long intervalDurationRightBound;
    Long intervalDurationStep;

    Double deviationLeftBound;
    Double deviationRightBound;
    Double deviationStep;
}
