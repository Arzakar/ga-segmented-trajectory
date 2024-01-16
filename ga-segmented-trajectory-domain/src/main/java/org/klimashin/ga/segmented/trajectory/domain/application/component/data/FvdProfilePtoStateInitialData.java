package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
public class FvdProfilePtoStateInitialData {

    UUID id;
    CelestialBodyData centralBody;
    List<CelestialBodyData> celestialBodies;
    SpacecraftData spacecraft;
    FvdCommandProfileSetup commandProfileSetup;
    FvdCommandProfileSnapshot lastCalculatedCommandProfile;
    PtoTargetStateSetup targetStateSetup;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
