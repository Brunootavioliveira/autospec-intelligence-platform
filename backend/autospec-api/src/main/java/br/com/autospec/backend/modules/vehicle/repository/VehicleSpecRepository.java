package br.com.autospec.backend.modules.vehicle.repository;


import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VehicleSpecRepository extends JpaRepository<VehicleSpec, Long> {
    Optional<VehicleSpec> findByBrandAndModelAndVersionAndYear(
            String brand,
            String model,
            String version,
            int year);

    @Modifying
    @Transactional
    @Query("DELETE FROM VehicleSpec v WHERE v.createdAt < :cutoffDate")
    int deleteSpecsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
