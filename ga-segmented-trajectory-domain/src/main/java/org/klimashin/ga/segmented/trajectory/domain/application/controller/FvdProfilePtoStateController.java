package org.klimashin.ga.segmented.trajectory.domain.application.controller;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateInitialData;
import org.klimashin.ga.segmented.trajectory.domain.application.facade.FvdProfilePtoStateFacade;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FvdProfilePtoStateController {

    FvdProfilePtoStateFacade facade;

    @PostMapping("/run")
    public void runCalc(@RequestBody FvdProfilePtoStateInitialData initialData) {
        facade.runCalculations(initialData);
    }
}
