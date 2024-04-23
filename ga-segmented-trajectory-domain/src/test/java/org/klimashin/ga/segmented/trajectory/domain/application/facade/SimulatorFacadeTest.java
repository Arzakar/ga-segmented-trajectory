package org.klimashin.ga.segmented.trajectory.domain.application.facade;

import static org.junit.jupiter.api.Assertions.*;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.CelestialBodyV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersRandomCreationRequestV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Pair;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Segment;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class SimulatorFacadeTest {

    @InjectMocks
    private SimulatorFacade simulatorFacade;

    @Test
    @Disabled
    void runRandomCalculations_shouldRun() {
        var randomCreationRequest = SimParametersRandomCreationRequestV1Dto.builder()
                .centralBodyName(CelestialBodyName.SOLAR)
                .celestialBodies(List.of(
                        CelestialBodyV1Dto.builder()
                                .name(CelestialBodyName.EARTH)
                                .anomaly(10.5d)
                                .build()
                )).spacecraft(SimParametersRandomCreationRequestV1Dto.SpacecraftRandomCreationRequestV1Dto.builder()
                        .posX(Pair.of(146100393440d, 146100393440d))
                        .posY(Pair.of(0d, 0d))
                        .spdX(Pair.of(0d, 0d))
                        .spdY(Pair.of(29600d, 30000d))
                        .mass(Pair.of(300d, 3000d))
                        .fuelMassPercent(Pair.of(0.1, 0.6))
                        .build()
                ).engine(SimParametersRandomCreationRequestV1Dto.EngineRandomCreationRequestV1Dto.builder()
                        .fuelConsumption(Pair.of(0.00002, 0.00006))
                        .thrust(Pair.of(0.100, 1.500))
                        .build()
                ).controlMinVariations(Map.of(
                        0, new Number[][] {{10, 20, 30}, {60d, 80d}},
                        1, new Number[][] {{70, 95}, {70d}}
                )).controlMaxVariations(Map.of(
                        0, new Number[][] {{15, 25, 35}, {65d, 85d}},
                        1, new Number[][] {{70, 100}, {70d}}
                )).build();

        simulatorFacade.runRandomCalculations(randomCreationRequest);
    }
}