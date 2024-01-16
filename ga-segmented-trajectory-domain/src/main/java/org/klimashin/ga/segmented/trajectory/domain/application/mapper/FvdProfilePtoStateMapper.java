package org.klimashin.ga.segmented.trajectory.domain.application.mapper;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateInitialData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateResultData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.FvdProfilePtoStateInitialEntity;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.FvdProfilePtoStateResultEntity;

import org.mapstruct.Mapper;

@Mapper
public interface FvdProfilePtoStateMapper {

    FvdProfilePtoStateInitialEntity dataToEntity(FvdProfilePtoStateInitialData data);

    FvdProfilePtoStateInitialData entityToData(FvdProfilePtoStateInitialEntity entity);

    FvdProfilePtoStateResultEntity dataToEntity(FvdProfilePtoStateResultData data);

    FvdProfilePtoStateResultData entityToData(FvdProfilePtoStateResultEntity entity);
}
