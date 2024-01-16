package org.klimashin.ga.segmented.trajectory.domain.application.controller;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.facade.InitialFacade;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InitialController {

    InitialFacade initialFacade;

    @Async
    @PostMapping("/initial/start")
    public void startCalculation(InitialCreationRequestDto creationRequestDto) {
        initialFacade.startCalculation(creationRequestDto);
    }
}
