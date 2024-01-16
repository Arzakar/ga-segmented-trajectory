package org.klimashin.ga.segmented.trajectory.domain.application.mapper;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.CelestialBodyData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.OrbitData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.SpacecraftData;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBody;
import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Orbit;
import org.klimashin.ga.segmented.trajectory.domain.model.component.Spacecraft;

import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper
public abstract class EnvironmentMapper {

    public abstract CelestialBody dataToModel(CelestialBodyData data);

    public Map<CelestialBodyName, CelestialBody> dataToModel(List<CelestialBodyData> data) {
        return data.stream().map(this::dataToModel)
                .collect(Collectors.toMap(CelestialBody::getName, Function.identity()));
    }

    public abstract CelestialBodyData modelToData(CelestialBody model);

    public List<CelestialBodyData> modelToData(Map<CelestialBodyName, CelestialBody> model) {
        return model.values().stream().map(this::modelToData).toList();
    }

    public abstract Orbit dataToModel(OrbitData orbitData);

    public abstract OrbitData modelToData(Orbit model);

    public abstract Spacecraft dataToModel(SpacecraftData data);

    public abstract SpacecraftData modelToData(Spacecraft model);
}
