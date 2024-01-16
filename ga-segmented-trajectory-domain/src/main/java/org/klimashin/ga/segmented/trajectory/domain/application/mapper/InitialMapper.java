package org.klimashin.ga.segmented.trajectory.domain.application.mapper;

import org.klimashin.ga.segmented.trajectory.domain.api.dto.InitialCreationRequestDto;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.InitialEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface InitialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "centralBody", ignore = true)
    @Mapping(target = "celestialBody", ignore = true)
    @Mapping(target = "lastCalculate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    InitialEntity creationRequestDtoToEntity(InitialCreationRequestDto requestDto);
}
