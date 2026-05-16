package br.com.autospec.backend.core.common;

import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataRetentionJob {

    private final VehicleSpecRepository vehicleSpecRepository;

    @Value("${app.retention.specs-months:6}")
    private int retentionMonths;

    @Scheduled(cron = "0 0 0 * * SUN")
    public void runDataCleanup() {
        log.info("Iniciando a rotina semanal de limpeza de dados expirados...");

        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(retentionMonths);

        try {
            int deletedRows = vehicleSpecRepository.deleteSpecsOlderThan(cutoffDate);
            log.info("Rotina de limpeza concluída com sucesso. Total de especificações antigas removidas: {}", deletedRows);
        } catch (Exception e) {
            log.error("Erro crítico ao executar a rotina de limpeza de dados em repouso.", e);
        }
    }
}
