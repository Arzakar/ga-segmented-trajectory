package org.klimashin.ga.segmented.trajectory.domain.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class SpacecraftV1Dto {

    @Schema(example = "146100393440")
    Double posX;

    @Schema(example = "0")
    Double posY;

    @Schema(example = "0.0")
    Double spdX;

    @Schema(example = "29818.407")
    Double spdY;

    @Schema(example = "1913.967")
    Double mass;

    @Schema(example = "1009.062")
    Double fuelMass;
}
