package org.klimashin.ga.segmented.trajectory.domain.application.controller;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.facade.SimulationFacade;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimulatorController {

    SimulationFacade simulationFacade;

    @Async
    @PostMapping("/simulator/start/random")
    public void startRandomCalculations() {
        simulationFacade.startRandomCalculations();
    }

    @Async
    @PostMapping(value = "/simulator/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startCalculation(InitialCreationRequestDto creationRequestDto) {
        simulationFacade.startCalculation(creationRequestDto);
    }

    @PostMapping("/simulation/calculate")
    public void calculateSimulation(@RequestBody SimParametersCreationRequestDto creationRequest) {
        simulationFacade.createSimParametersAndStartCalculation(creationRequest);
    }

    @Async
    @PostMapping("/simulator/{id}/resume")
    public void startCalculation(@PathVariable("id") UUID id) {
        simulationFacade.resumeCalculation(id);
    }
}
