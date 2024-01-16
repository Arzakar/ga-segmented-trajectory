package org.klimashin.ga.segmented.trajectory.domain.application.repository;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSnapshot;
import org.klimashin.ga.segmented.trajectory.domain.application.component.entity.FvdProfilePtoStateInitialEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface FvdProfilePtoStateInitialRepository extends JpaRepository<FvdProfilePtoStateInitialEntity, UUID> {

    @Modifying
    @Transactional
    @Query("""
            UPDATE FvdProfilePtoStateInitialEntity fppsi
            SET fppsi.commandProfileSetup = :commandProfile
            WHERE fppsi.id = :id
            """)
    void updateLastCalculatedCommandProfile(@Param("id") UUID id, @Param("commandProfile") FvdCommandProfileSnapshot lastCalculatedCommandProfile);
}
