package org.klimashin.ga.segmented.trajectory.domain.application.mapper;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.SimParametersCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.SimParametersEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SimParametersMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "centralBody", ignore = true)
    @Mapping(target = "lastCalculatedSim", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SimParametersEntity creationRequestDtoToEntity(SimParametersCreationRequestDto creationRequestDto);
}
