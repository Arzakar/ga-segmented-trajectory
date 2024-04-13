package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@AllArgsConstructor
public class ApplicableSimResult {

    UUID simParametersId;
    UUID simResultId;
    Double scStartPosX;
    Double scStartPosY;
    Double scStartSpdX;
    Double scStartSpdY;
    Double scStartMass;
    Double scStartFuelMass;
    Double engConsumption;
    Double engThrust;
    Double scResPosX;
    Double scResPosY;
    Double scResSpdX;
    Double scResSpdY;
    Double scResMass;
    Double scResFuelMass;
    Double interval1;
    Double deviation1;
    Double interval2;
    Double deviation2;
    Long e2eDuration;
    Double leftResultApocenter;
    Double rightResultApocenter;
}
