package br.com.autospec.backend.modules.garage.controller;

import br.com.autospec.backend.modules.garage.dto.*;
import br.com.autospec.backend.modules.garage.entity.FleetType;
import br.com.autospec.backend.modules.garage.service.GarageService;
import br.com.autospec.backend.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/garage")
@RequiredArgsConstructor
@Tag(name = "Garage", description = "Gerenciamento da frota pessoal/profissional do usuário")
@SecurityRequirement(name = "bearerAuth")
public class GarageController {
    private final GarageService garageService;

    @PostMapping
    @Operation(summary = "Adicionar veículo à garage")
    public ResponseEntity<GarageVehicleResponseDTO> addVehicle(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddVehicleToGarageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(garageService.addVehicle(user, request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os veículos da garage")
    public ResponseEntity<List<GarageVehicleResponseDTO>> listAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(garageService.listAll(user));
    }

    @GetMapping("/fleet/{fleetType}")
    @Operation(summary = "Listar por tipo de frota (PERSONAL ou WORK)")
    public ResponseEntity<List<GarageVehicleResponseDTO>> listByFleet(
            @AuthenticationPrincipal User user,
            @PathVariable FleetType fleetType) {
        return ResponseEntity.ok(garageService.listByFleet(user, fleetType));
    }

    @GetMapping("/insights")
    @Operation(summary = "Insights da garage (total, mais potente, etc)")
    public ResponseEntity<GarageInsightsDTO> getInsights(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(garageService.getInsights(user));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar fleet type ou nickname de um veículo da garage")
    public ResponseEntity<GarageVehicleResponseDTO> updateVehicle(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody UpdateGarageVehicleRequestDTO request) {
        return ResponseEntity.ok(garageService.updateVehicle(user, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover veículo da garage (soft delete)")
    public ResponseEntity<Void> removeVehicle(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        garageService.removeVehicle(user, id);
        return ResponseEntity.noContent().build();
    }
}
