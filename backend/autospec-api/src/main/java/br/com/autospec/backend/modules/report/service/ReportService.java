package br.com.autospec.backend.modules.report.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.report.dto.ComparisonReportRequestDTO;
import br.com.autospec.backend.modules.report.dto.ReportMetadataDTO;
import br.com.autospec.backend.modules.report.dto.VehicleDossierRequestDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleCompareResponseDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import br.com.autospec.backend.modules.vehicle.mapper.VehicleSpecMapper;
import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import br.com.autospec.backend.modules.vehicle.service.VehicleComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Set<String> ALLOWED_PARAMS =
            Set.of("ENGINE", "PERFORMANCE", "PRICE", "SAFETY", "DIMENSIONS");

    private final Map<String, byte[]> pdfCache = new ConcurrentHashMap<>();

    private final VehicleSpecRepository     vehicleSpecRepository;
    private final VehicleSpecMapper         vehicleSpecMapper;
    private final VehicleComparisonService  vehicleComparisonService;
    private final PdfReportGenerator        pdfReportGenerator;

    @Transactional(readOnly = true)
    public ReportMetadataDTO generateComparisonReport(ComparisonReportRequestDTO request) {
        validateParams(request.parameters());

        VehicleSpec vA = vehicleSpecRepository.findById(request.vehicleAId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo A não encontrado"));
        VehicleSpec vB = vehicleSpecRepository.findById(request.vehicleBId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo B não encontrado"));

        VehicleCompareResponseDTO comparison =
                vehicleComparisonService.compare(request.vehicleAId(), request.vehicleBId());

        byte[] pdfBytes = pdfReportGenerator.generateComparisonPdf(comparison);

        String reportId   = UUID.randomUUID().toString();
        String title      = "Comprehensive Comparison Report: "
                + vA.getModel() + " vs " + vB.getModel();
        String downloadUrl = "/api/v1/reports/" + reportId + "/download";

        pdfCache.put(reportId, pdfBytes);

        return new ReportMetadataDTO(reportId, "COMPARISON", title, LocalDateTime.now(), downloadUrl);
    }

    @Transactional(readOnly = true)
    public ReportMetadataDTO generateDossier(VehicleDossierRequestDTO request) {
        VehicleSpec spec = vehicleSpecRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        VehicleResponseDTO dto = vehicleSpecMapper.toResponse(spec);
        byte[] pdfBytes = pdfReportGenerator.generateDossierPdf(dto);

        String reportId   = UUID.randomUUID().toString();
        String title      = "Vehicle History Dossier: "
                + spec.getBrand() + " " + spec.getModel() + " (" + spec.getYear() + ")";
        String downloadUrl = "/api/v1/reports/" + reportId + "/download";

        pdfCache.put(reportId, pdfBytes);

        return new ReportMetadataDTO(reportId, "DOSSIER", title, LocalDateTime.now(), downloadUrl);
    }

    public byte[] downloadReport(String reportId) {
        byte[] bytes = pdfCache.remove(reportId);
        if (bytes == null) {
            throw new ResourceNotFoundException(
                    "Relatório não encontrado ou já baixado. Gere novamente.");
        }
        return bytes;
    }

    private void validateParams(Set<String> params) {
        if (params == null || params.isEmpty()) return;
        params.forEach(p -> {
            if (!ALLOWED_PARAMS.contains(p.toUpperCase())) {
                throw new BusinessException(
                        "Parâmetro inválido: " + p + ". Permitidos: " + ALLOWED_PARAMS);
            }
        });
    }
}
