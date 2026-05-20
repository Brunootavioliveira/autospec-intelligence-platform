package br.com.autospec.backend.modules.analysis.controller;

import br.com.autospec.backend.modules.analysis.dto.VehicleAnalysisDTO;
import br.com.autospec.backend.modules.analysis.service.VehicleAnalysisService;
import br.com.autospec.backend.modules.history.entity.ActionType;
import br.com.autospec.backend.modules.history.service.UserHistoryService;
import br.com.autospec.backend.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Análise de veículos com métricas derivadas e percentis")
@SecurityRequirement(name = "bearerAuth")
public class VehicleAnalysisController {

    private final VehicleAnalysisService analysisService;
    private final UserHistoryService historyService;

    @GetMapping("/{vehicleId}")
    @Operation(summary = "Análise completa: power-to-weight, track handling score e percentis populacionais")
    public ResponseEntity<VehicleAnalysisDTO> analyze(
            @AuthenticationPrincipal User user,
            @PathVariable Long vehicleId) {

        VehicleAnalysisDTO result = analysisService.analyze(vehicleId);

        historyService.record(
                user,
                ActionType.ANALYSIS,
                "Vehicle Analysis: " + result.vehicle().brand() + " " + result.vehicle().model()
                        + " (" + result.vehicle().year() + ")",
                "Power-to-Weight: " + result.powerToWeightKgKw()
                        + " kg/kW | Track Handling Score: " + result.trackHandlingScore(),
                vehicleId
        );

        return ResponseEntity.ok(result);
    }
}
