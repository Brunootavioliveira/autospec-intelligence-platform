package br.com.autospec.backend.repository;


import br.com.autospec.backend.entity.VehicleSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleSpecRepository extends JpaRepository<VehicleSpec, Long> {
    Optional<VehicleSpec> findByBrandAndModelAndVersionAndYear(
            String brand,
            String model,
            String version,
            int year);
}
