package br.com.autospec.backend.modules.report.controller;

import br.com.autospec.backend.modules.history.entity.ActionType;
import br.com.autospec.backend.modules.history.service.UserHistoryService;
import br.com.autospec.backend.modules.report.dto.ComparisonReportRequestDTO;
import br.com.autospec.backend.modules.report.dto.ReportMetadataDTO;
import br.com.autospec.backend.modules.report.dto.VehicleDossierRequestDTO;
import br.com.autospec.backend.modules.report.service.ReportService;
import br.com.autospec.backend.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Geração de relatórios PDF: comparação e dossier")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;
    private final UserHistoryService historyService;

    @PostMapping("/comparison")
    @Operation(
            summary = "Gerar Comprehensive Comparison Report (PDF)",
            description = "Parâmetros aceitos em `parameters`: ENGINE, PERFORMANCE, PRICE, SAFETY, DIMENSIONS. " +
                    "Omitir o campo inclui todos. Retorna metadata com `downloadUrl`."
    )
    public ResponseEntity<ReportMetadataDTO> generateComparisonReport(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ComparisonReportRequestDTO request) {

        ReportMetadataDTO report = reportService.generateComparisonReport(request);

        historyService.record(
                user,
                ActionType.COMPARISON,
                report.title(),
                "Relatório PDF gerado — parâmetros: " +
                        (request.parameters() == null || request.parameters().isEmpty()
                                ? "todos" : String.join(", ", request.parameters())),
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @PostMapping("/dossier")
    @Operation(
            summary = "Gerar Vehicle History Dossier (PDF)",
            description = "Relatório detalhado de um único veículo com specs completas. Retorna metadata com `downloadUrl`."
    )
    public ResponseEntity<ReportMetadataDTO> generateDossier(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VehicleDossierRequestDTO request) {

        ReportMetadataDTO report = reportService.generateDossier(request);

        historyService.record(
                user,
                ActionType.ANALYSIS,
                report.title(),
                "Dossier PDF gerado",
                request.vehicleId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(report);
    }

    @GetMapping("/{reportId}/download")
    @Operation(
            summary = "Download do PDF gerado",
            description = "Retorna o PDF como application/pdf. O link expira após o primeiro download — gere novamente se necessário."
    )
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal User user,
            @PathVariable String reportId) {

        byte[] pdfBytes = reportService.downloadReport(reportId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"autospec-report-" + reportId + ".pdf\"")
                .body(pdfBytes);
    }
}
