package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.CelestialBodyData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSetup;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSnapshot;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.PtoTargetStateSetup;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.SpacecraftData;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fvd_profile_pto_state_initial")

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FvdProfilePtoStateInitialEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "central_body", nullable = false, updatable = false)
    CelestialBodyData centralBody;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "celestial_bodies", nullable = false, updatable = false)
    List<CelestialBodyData> celestialBodies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spacecraft", nullable = false, updatable = false)
    SpacecraftData spacecraft;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "command_profile_setup", nullable = false, updatable = false)
    FvdCommandProfileSetup commandProfileSetup;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_calculated_command_profile")
    FvdCommandProfileSnapshot lastCalculatedCommandProfile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_state_setup", nullable = false, updatable = false)
    PtoTargetStateSetup targetStateSetup;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    OffsetDateTime updatedAt;
}
