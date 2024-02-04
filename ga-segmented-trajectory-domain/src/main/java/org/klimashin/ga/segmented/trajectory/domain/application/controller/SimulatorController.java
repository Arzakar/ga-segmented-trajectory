package org.klimashin.ga.segmented.trajectory.domain.application.controller;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.facade.SimulatorFacade;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SimulatorController {

    SimulatorFacade simulatorFacade;

    @Async
    @PostMapping("/simulator/start/random")
    public void startRandomCalculations() {
        simulatorFacade.startRandomCalculations();
    }

    @Async
    @PostMapping("/simulator/start")
    public void startCalculation(InitialCreationRequestDto creationRequestDto) {
        simulatorFacade.startCalculation(creationRequestDto);
    }

    @Async
    @PostMapping("/simulator/{id}/resume")
    public void startCalculation(@PathVariable("id") UUID id) {
        simulatorFacade.resumeCalculation(id);
    }
}
