package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.InitialEntity;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.Builder;
import lombok.Value;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for {@link InitialEntity}
 */
@Value
@Builder
public class InitialCreationRequestDto {

    @Schema(example = "SOLAR")
    CelestialBodyName centralBodyName;

    @Schema(example = "EARTH")
    CelestialBodyName celestialBodyName;

    @Schema(example = "0")
    Double celestialBodyAnomaly;

    @Schema(example = "1.4610039344E11")
    Double spacecraftPosX;

    @Schema(example = "0")
    Double spacecraftPosY;

    @Schema(example = "0")
    Double spacecraftSpdX;

    @Schema(example = "29784")
    Double spacecraftSpdY;

    @Schema(example = "0.000010")
    Double fuelConsumption;

    @Schema(example = "350")
    Double mass;

    @Schema(example = "100")
    Double fuelMass;

    @Schema(example = "0.220")
    Double thrust;

    @Schema(example = "10")
    Long intervalLeftBound;

    @Schema(example = "180")
    Long intervalRightBound;

    @Schema(example = "10")
    Long intervalStep;

    @Schema(example = "-180")
    Double deviationLeftBound;

    @Schema(example = "180")
    Double deviationRightBound;

    @Schema(example = "10")
    Double deviationStep;

    @Schema(example = "1e+9")
    Double requiredDistance;
}