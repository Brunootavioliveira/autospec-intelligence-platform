package br.com.autospec.backend.repository;


import br.com.autospec.backend.entity.VehicleSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleSpecRepository extends JpaRepository<VehicleSpec, Long> {
}
