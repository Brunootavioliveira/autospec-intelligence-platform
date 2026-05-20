package br.com.autospec.backend.modules.garage.repository;

import br.com.autospec.backend.modules.garage.entity.FleetType;
import br.com.autospec.backend.modules.garage.entity.GarageVehicle;
import br.com.autospec.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GarageVehicleRepository extends JpaRepository<GarageVehicle, Long> {
    List<GarageVehicle> findByUserAndActiveTrue(User user);

    List<GarageVehicle> findByUserAndFleetTypeAndActiveTrue(User user, FleetType fleetType);

    Optional<GarageVehicle> findByIdAndUser(Long id, User user);

    boolean existsByUserAndVehicleSpecIdAndActiveTrue(User user, Long vehicleSpecId);
}
