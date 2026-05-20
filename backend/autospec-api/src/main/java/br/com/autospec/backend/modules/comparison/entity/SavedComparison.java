package br.com.autospec.backend.modules.comparison.entity;

import br.com.autospec.backend.core.common.Auditable;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saved_comparisons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedComparison extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_a_id", nullable = false)
    private VehicleSpec vehicleA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_b_id", nullable = false)
    private VehicleSpec vehicleB;

    @Column(length = 255)
    private String title;
}
