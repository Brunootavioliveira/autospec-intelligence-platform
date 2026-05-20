package br.com.autospec.backend.modules.report.dto;

import java.time.LocalDateTime;

public record ReportMetadataDTO(
        String reportId,
        String reportType,
        String title,
        LocalDateTime generatedAt,
        String downloadUrl
) {
}
