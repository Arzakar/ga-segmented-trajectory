package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class SimParametersCreationRequestDto {

    @Schema(example = "SOLAR")
    CelestialBodyName centralBody;

    @Schema(example = "{\"EARTH\": 0}")
    Map<CelestialBodyName, Double> celestialBodiesByAnomalies;

    @Schema(example = "[146100393440, 0]")
    Double[] spacecraftPos;

    @Schema(example = "[0.0, 29818.407]")
    Double[] spacecraftSpd;

    @Schema(example = "1913.967")
    Double spacecraftMass;

    @Schema(example = "1009.062")
    Double spacecraftFuelMass;

    @Schema(example = "4.317E-5")
    Double engineFuelConsumption;

    @Schema(example = "0.828")
    Double engineThrust;

    @Schema(example = "{\"0\": [[10, 30], [14.918, 80.885]], \"1\": [[70, 170], [113.869, 179.836]]}")
    Map<Integer, Number[][]> controlVariations;
}
