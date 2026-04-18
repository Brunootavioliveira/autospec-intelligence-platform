package br.com.autospec.backend.controller;

import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping("/vehicles/spec")
    public VehicleResponseDTO generateVehicleSpec(@Valid @RequestBody VehicleRequestDTO request) {
        return vehicleService.generateVehicleSpec(request);
    }
}
