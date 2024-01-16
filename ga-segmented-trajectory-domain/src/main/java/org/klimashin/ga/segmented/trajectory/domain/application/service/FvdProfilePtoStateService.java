package org.klimashin.ga.segmented.trajectory.domain.application.service;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSnapshot;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateInitialData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdProfilePtoStateResultData;
import org.klimashin.ga.segmented.trajectory.domain.application.mapper.FvdProfilePtoStateMapper;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.FvdProfilePtoStateInitialRepository;
import org.klimashin.ga.segmented.trajectory.domain.application.repository.FvdProfilePtoStateResultRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FvdProfilePtoStateService {

    FvdProfilePtoStateInitialRepository initialRepository;
    FvdProfilePtoStateResultRepository resultRepository;
    FvdProfilePtoStateMapper mapper;

    @Transactional
    public FvdProfilePtoStateInitialData saveInitial(FvdProfilePtoStateInitialData initialData) {
        var savedInitial = initialRepository.saveAndFlush(mapper.dataToEntity(initialData));
        return mapper.entityToData(savedInitial);
    }

    @Transactional
    public void updateLastCalculatedCommandProfile(UUID id, FvdCommandProfileSnapshot commandProfileSnapshot) {
        initialRepository.updateLastCalculatedCommandProfile(id, commandProfileSnapshot);
    }

    @Transactional
    public FvdProfilePtoStateResultData saveResult(FvdProfilePtoStateResultData resultData) {
        var savedResult = resultRepository.saveAndFlush(mapper.dataToEntity(resultData));
        return mapper.entityToData(savedResult);
    }
}
