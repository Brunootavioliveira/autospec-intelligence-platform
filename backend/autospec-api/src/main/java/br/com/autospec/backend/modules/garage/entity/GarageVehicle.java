package br.com.autospec.backend.modules.garage.entity;

import br.com.autospec.backend.core.common.Auditable;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "garage_vehicles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GarageVehicle extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_spec_id", nullable = false)
    private VehicleSpec vehicleSpec;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FleetType fleetType;

    @Column(length = 255)
    private String nickname;

    @Column(nullable = false)
    private boolean active;
}
