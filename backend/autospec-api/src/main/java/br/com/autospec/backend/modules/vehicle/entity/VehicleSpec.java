package br.com.autospec.backend.modules.vehicle.entity;

import br.com.autospec.backend.core.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_specs", uniqueConstraints = {
        @UniqueConstraint(columnNames =
                {"brand", "model", "version", "year"})})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class VehicleSpec extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String model;

    @Column(length = 100)
    private String version;

    @Column(length = 100)
    private Integer year;

    private String engine;
    private Integer horsepower;
    private BigDecimal torque;
    private String drivetrain;
    private Integer topSpeed;
    private BigDecimal acceleration;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer electricRange;
    private BigDecimal price;
}
