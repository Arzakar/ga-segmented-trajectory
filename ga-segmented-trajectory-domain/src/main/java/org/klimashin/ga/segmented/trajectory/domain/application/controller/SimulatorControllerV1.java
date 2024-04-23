package org.klimashin.ga.segmented.trajectory.domain.application.controller;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersCreationRequestV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersRandomCreationRequestV1Dto;
import org.klimashin.ga.segmented.trajectory.domain.application.facade.SimulatorFacade;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimulatorControllerV1 {

    SimulatorFacade simulatorFacade;

    @Async
    @PostMapping("/simulator/start-calculate")
    public void calculateSimulation(@RequestBody SimParametersCreationRequestV1Dto creationRequest) {
        simulatorFacade.createSimParametersAndStartCalculation(creationRequest);
    }

    @Async
    @PostMapping("/simulator/start-random-calculate")
    public void calculateRandomSimulations(@RequestBody SimParametersRandomCreationRequestV1Dto randomCreationRequest) {
        simulatorFacade.runRandomCalculations(randomCreationRequest);
    }
}
