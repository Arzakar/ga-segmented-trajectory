package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class PtoTargetStateSetup {

    CelestialBodyName targetObjectName;
    Double requiredDistance;
}
