package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@AllArgsConstructor
public class FvdCommandProfileSnapshot {

    Map<Integer, Long> intervalsByDurations;
    Map<Integer, Double> intervalsByDeviations;
}
