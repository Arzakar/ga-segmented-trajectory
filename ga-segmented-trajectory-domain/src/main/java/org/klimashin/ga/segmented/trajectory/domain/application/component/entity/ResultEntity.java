package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "result")

@Data
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id")
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JdbcTypeCode(SqlTypes.UUID)
    @JoinColumn(name = "initial_id", nullable = false, updatable = false)
    InitialEntity initialEntity;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "celestial_body_anomaly", nullable = false, updatable = false)
    Double celestialBodyAnomaly;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_pos_x", nullable = false, updatable = false)
    Double spacecraftPosX;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_pos_y", nullable = false, updatable = false)
    Double spacecraftPosY;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_spd_x", nullable = false, updatable = false)
    Double spacecraftSpdX;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_spd_y", nullable = false, updatable = false)
    Double spacecraftSpdY;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_mass", nullable = false, updatable = false)
    Double mass;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "spacecraft_fuel_mass", nullable = false, updatable = false)
    Double fuelMass;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "first_interval", nullable = false, updatable = false)
    Long firstInterval;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "first_deviation", nullable = false, updatable = false)
    Double firstDeviation;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "second_interval", nullable = false, updatable = false)
    Long secondInterval;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "second_deviation", nullable = false, updatable = false)
    Double secondDeviation;

    @JdbcTypeCode(SqlTypes.BOOLEAN)
    @Column(name = "is_complete", nullable = false, updatable = false)
    Boolean isComplete;

    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "duration", nullable = false, updatable = false)
    Long duration;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "result_apocenter", updatable = false)
    Double resultApocenter;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_WITH_TIMEZONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;
}
