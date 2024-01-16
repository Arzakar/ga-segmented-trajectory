package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import org.klimashin.ga.segmented.trajectory.domain.application.component.data.CelestialBodyData;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSetup;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.FvdCommandProfileSnapshot;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.PtoTargetStateSetup;
import org.klimashin.ga.segmented.trajectory.domain.application.component.data.SpacecraftData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import liquibase.sql.Sql;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fvd_profile_pto_state_result")

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FvdProfilePtoStateResultEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    @EqualsAndHashCode.Exclude
    UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "initial_id", nullable = false, updatable = false)
    UUID initialId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "celestial_bodies", nullable = false, updatable = false)
    List<CelestialBodyData> celestialBodies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spacecraft", nullable = false, updatable = false)
    SpacecraftData spacecraft;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculated_command_profile", nullable = false, updatable = false)
    FvdCommandProfileSnapshot calculatedCommandProfile;

    @JdbcTypeCode(SqlTypes.BOOLEAN)
    @Column(name = "target_state_is_achieved", nullable = false, updatable = false)
    Boolean targetStateIsAchieved;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "duration", nullable = false, updatable = false)
    Long duration;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;
}
