package org.klimashin.ga.segmented.trajectory.domain.application.component.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orbit")

@Data
@NoArgsConstructor
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrbitEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id")
    String id;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "apocenter")
    Double apocenter;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "pericenter")
    Double pericenter;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "semi_major_axis")
    Double semiMajorAxis;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "eccentricity")
    Double eccentricity;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "inclination")
    Double inclination;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "longitude_asc_node")
    Double longitudeAscNode;

    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Column(name = "perihelion_argument")
    Double perihelionArgument;

    @ManyToOne(optional = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @JoinColumn(name = "attracting_body_id", nullable = false)
    CelestialBodyEntity attractingBody;
}
