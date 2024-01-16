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
public class FvdProfilePtoStateResultData {

    UUID id;
    UUID initialId;
    List<CelestialBodyData> celestialBodies;
    SpacecraftData spacecraft;
    FvdCommandProfileSnapshot calculatedCommandProfile;
    Boolean targetStateIsAchieved;
    Long duration;
    OffsetDateTime createdAt;
}
