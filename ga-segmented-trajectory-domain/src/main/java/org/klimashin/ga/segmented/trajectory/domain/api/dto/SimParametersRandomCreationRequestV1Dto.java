package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class SimParametersRandomCreationRequestV1Dto {

    @Schema(example = "SOLAR")
    CelestialBodyName centralBodyName;

    List<CelestialBodyV1Dto> celestialBodies;

    SpacecraftRandomCreationRequestV1Dto spacecraft;

    EngineRandomCreationRequestV1Dto engine;

    @Schema(example = "{\"0\": [[10, 30], [14.918, 80.885]], \"1\": [[70, 170], [113.869, 179.836]]}")
    Map<Integer, Number[][]> controlMinVariations;

    @Schema(example = "{\"0\": [[20, 50], [17.918, 85.885]], \"1\": [[80, 190], [115.869, 181.836]]}")
    Map<Integer, Number[][]> controlMaxVariations;

    @Value
    @Builder
    @Jacksonized
    @AllArgsConstructor
    public static class SpacecraftRandomCreationRequestV1Dto {

        @Schema(example = "146100393440")
        Double posXMin;

        @Schema(example = "146100393440")
        Double posXMax;

        @Schema(example = "0")
        Double posYMin;

        @Schema(example = "0")
        Double posYMax;

        @Schema(example = "0.0")
        Double spdXMin;

        @Schema(example = "0.0")
        Double spdXMax;

        @Schema(example = "29818.407")
        Double spdYMin;

        @Schema(example = "29818.407")
        Double spdYMax;

        @Schema(example = "1913.967")
        Double massMin;

        @Schema(example = "1913.967")
        Double massMax;

        @Schema(example = "10.5")
        Double fuelMassPercentMin;

        @Schema(example = "10.5")
        Double fuelMassPercentMax;
    }

    @Value
    @Builder
    @Jacksonized
    @AllArgsConstructor
    public static class EngineRandomCreationRequestV1Dto {

        @Schema(example = "4.317E-5")
        Double fuelConsumptionMin;

        @Schema(example = "4.317E-5")
        Double fuelConsumptionMax;

        @Schema(example = "0.828")
        Double thrustMin;

        @Schema(example = "0.828")
        Double thrustMax;
    }
}
