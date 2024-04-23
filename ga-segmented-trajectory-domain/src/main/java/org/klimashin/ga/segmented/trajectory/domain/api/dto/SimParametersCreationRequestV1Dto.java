package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class SimParametersCreationRequestV1Dto {

    @Schema(example = "SOLAR")
    CelestialBodyName centralBodyName;

    List<CelestialBodyV1Dto> celestialBodies;

    SpacecraftV1Dto spacecraft;

    EngineV1Dto engine;

    @Schema(example = "{\"0\": [[10, 30], [14.918, 80.885]], \"1\": [[70, 170], [113.869, 179.836]]}")
    Map<Integer, Number[][]> controlVariations;
}
