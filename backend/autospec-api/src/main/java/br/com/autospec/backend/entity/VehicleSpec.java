package br.com.autospec.backend.entity;

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
public class VehicleSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private String version;
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
