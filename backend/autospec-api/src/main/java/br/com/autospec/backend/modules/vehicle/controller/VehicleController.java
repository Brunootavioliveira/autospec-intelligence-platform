package br.com.autospec.backend.modules.vehicle.controller;

import br.com.autospec.backend.modules.vehicle.dto.VehicleCompareRequestDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleCompareResponseDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleRequestDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import br.com.autospec.backend.modules.vehicle.service.VehicleComparisonService;
import br.com.autospec.backend.modules.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicles/spec")
@RequiredArgsConstructor
@Tag(name = "Vehicle Specs", description = "Geração e consulta de especificações via IA")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleComparisonService vehicleComparisonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Gerar especificação via IA",
            description = "Busca specs do veículo na IA e persiste no banco. Roles: ANALYST, ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Spec gerada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão")
    })
    public ResponseEntity<VehicleResponseDTO> generateVehicleSpec(@Valid @RequestBody VehicleRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.generateVehicleSpec(request));
    }

    @PostMapping("/compare")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Compare two vehicles",
            description = "Compares two vehicles using brand, model, version and year"
    )
    public ResponseEntity<VehicleCompareResponseDTO> compareVehicles(@Valid @RequestBody VehicleCompareRequestDTO request) {

        return ResponseEntity.ok(
                vehicleComparisonService.compare(request)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar spec por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "404", description = "Não encontrado")
    })
    public ResponseEntity<VehicleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.findById(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todas as specs paginado")
    public ResponseEntity<Page<VehicleResponseDTO>> findAll(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(vehicleService.findAll(pageable));
    }


    @GetMapping("/compare")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Comparar dois veículos",
            description = """
        Compara especificações técnicas de dois veículos pelo ID.
        
        Retorna:
        - vencedor geral
        - placar da comparação
        - vencedor por atributo
        - valores individuais de cada veículo
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "IDs inválidos ou iguais"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<VehicleCompareResponseDTO> compare(@RequestParam Long idA, @RequestParam Long idB) {
        return ResponseEntity.ok(
                vehicleComparisonService.compare(idA, idB)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar spec",
            description = "Apenas ADMIN pode deletar. Role: ADMIN")
    @ApiResponse(responseCode = "204", description = "Deletado com sucesso")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
