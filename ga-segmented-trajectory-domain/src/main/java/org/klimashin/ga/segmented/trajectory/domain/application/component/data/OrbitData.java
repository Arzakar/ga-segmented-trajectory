package org.klimashin.ga.segmented.trajectory.domain.application.component.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class OrbitData {

    CelestialBodyData attractingBody;
    Double apocenter;
    Double pericenter;
    Double semiMajorAxis;
    Double eccentricity;
    Double inclination;
    Double longitudeAscNode;
    Double perihelionArgument;
    Double trueAnomaly;
    Long zeroEpoch;
}
