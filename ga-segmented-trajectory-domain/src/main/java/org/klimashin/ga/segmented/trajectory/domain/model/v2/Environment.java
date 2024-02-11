package org.klimashin.ga.segmented.trajectory.domain.model.v2;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Environment {

    CelestialBody centralBody;

    Map<CelestialBodyName, CelestialBody> celestialBodies;

}
