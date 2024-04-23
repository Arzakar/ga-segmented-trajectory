package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Pair;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Segment;

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

        Pair<Double, Double> posX;

        Pair<Double, Double> posY;

        Pair<Double, Double> spdX;

        Pair<Double, Double> spdY;

        Pair<Double, Double> mass;

        Pair<Double, Double> fuelMassPercent;
    }

    @Value
    @Builder
    @Jacksonized
    @AllArgsConstructor
    public static class EngineRandomCreationRequestV1Dto {

        Pair<Double, Double> fuelConsumption;

        Pair<Double, Double> thrust;
    }
}
