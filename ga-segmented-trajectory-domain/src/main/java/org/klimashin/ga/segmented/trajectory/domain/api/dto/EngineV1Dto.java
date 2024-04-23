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
public class EngineV1Dto {

    @Schema(example = "4.317E-5")
    Double fuelConsumption;

    @Schema(example = "0.828")
    Double thrust;
}
