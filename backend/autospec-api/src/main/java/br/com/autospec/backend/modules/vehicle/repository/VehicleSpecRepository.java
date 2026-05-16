package br.com.autospec.backend.modules.vehicle.repository;


import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleSpecRepository extends JpaRepository<VehicleSpec, Long> {
    Optional<VehicleSpec> findByBrandAndModelAndVersionAndYear(
            String brand,
            String model,
            String version,
            int year);
}
