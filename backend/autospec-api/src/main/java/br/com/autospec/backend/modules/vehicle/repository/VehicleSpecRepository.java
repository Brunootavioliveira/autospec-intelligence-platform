package br.com.autospec.backend.modules.vehicle.repository;


import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface VehicleSpecRepository extends JpaRepository<VehicleSpec, Long> {
    Optional<VehicleSpec> findByBrandAndModelAndVersionAndYear(
            String brand,
            String model,
            String version,
            int year);


    @Query(value = """
    SELECT * FROM vehicle_specs v
    WHERE (CAST(:q AS text) IS NULL
           OR LOWER(v.brand)   LIKE LOWER(CONCAT('%', CAST(:q AS text), '%'))
           OR LOWER(v.model)   LIKE LOWER(CONCAT('%', CAST(:q AS text), '%'))
           OR LOWER(v.version) LIKE LOWER(CONCAT('%', CAST(:q AS text), '%')))
    AND (CAST(:brand AS text) IS NULL OR LOWER(v.brand) = LOWER(CAST(:brand AS text)))
    AND (CAST(:minYear AS integer) IS NULL OR v.year >= CAST(:minYear AS integer))
    AND (CAST(:maxYear AS integer) IS NULL OR v.year <= CAST(:maxYear AS integer))
    AND (CAST(:minHp AS integer) IS NULL OR v.horsepower >= CAST(:minHp AS integer))
    AND (CAST(:maxHp AS integer) IS NULL OR v.horsepower <= CAST(:maxHp AS integer))
    """, nativeQuery = true)
    Page<VehicleSpec> search(
            @Param("q")       String q,
            @Param("brand")   String brand,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("minHp")   Integer minHp,
            @Param("maxHp")   Integer maxHp,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM VehicleSpec v WHERE v.createdAt < :cutoffDate")
    int deleteSpecsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
