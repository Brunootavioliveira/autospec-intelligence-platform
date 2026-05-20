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


    @Query("""
        SELECT v FROM VehicleSpec v
        WHERE (:q IS NULL OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :q, '%'))
                          OR LOWER(v.model) LIKE LOWER(CONCAT('%', :q, '%'))
                          OR LOWER(v.version) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:brand IS NULL OR LOWER(v.brand) = LOWER(:brand))
        AND (:minYear IS NULL OR v.year >= :minYear)
        AND (:maxYear IS NULL OR v.year <= :maxYear)
        AND (:minHp IS NULL OR v.horsepower >= :minHp)
        AND (:maxHp IS NULL OR v.horsepower <= :maxHp)
    """)
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
