package br.com.autospec.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_specs")
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
    private int year;

    private String engine;
    private int horsepower;
    private int torque;
    private String drivetrain;
    private double price;
}
