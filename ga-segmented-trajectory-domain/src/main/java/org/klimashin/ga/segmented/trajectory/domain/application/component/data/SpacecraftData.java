package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import org.klimashin.ga.segmented.trajectory.domain.util.component.Point;
import org.klimashin.ga.segmented.trajectory.domain.util.component.Vector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class SpacecraftData {

    Point position;
    Vector speed;
    Double fuelConsumption;
    Double mass;
    Double fuelMass;
    Double thrust;
}
