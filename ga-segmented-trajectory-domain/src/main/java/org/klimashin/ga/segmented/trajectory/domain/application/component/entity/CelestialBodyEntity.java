package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import org.klimashin.ga.segmented.trajectory.domain.model.component.CelestialBodyName;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "celestial_body")

@Data
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CelestialBodyEntity {

    @Id
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "name")
    CelestialBodyName name;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "mass")
    Double mass;

    @OneToOne(cascade = CascadeType.ALL)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @JoinColumn(name = "orbit_id")
    @EqualsAndHashCode.Exclude
    OrbitEntity orbit;
}
