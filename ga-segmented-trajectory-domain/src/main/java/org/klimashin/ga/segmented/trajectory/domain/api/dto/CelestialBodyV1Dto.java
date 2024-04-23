package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class CelestialBodyV1Dto {

    CelestialBodyName name;

    Double anomaly;
}
