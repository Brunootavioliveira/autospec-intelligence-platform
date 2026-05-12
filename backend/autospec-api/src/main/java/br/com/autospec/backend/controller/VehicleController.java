package br.com.autospec.backend.controller;

import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles/spec")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> generateVehicleSpec(@Valid @RequestBody VehicleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.generateVehicleSpec(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponseDTO>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(vehicleService.findAll(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
