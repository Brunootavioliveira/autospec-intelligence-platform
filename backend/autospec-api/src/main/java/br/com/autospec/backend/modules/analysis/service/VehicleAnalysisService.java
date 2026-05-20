package br.com.autospec.backend.modules.analysis.service;

import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.analysis.dto.VehicleAnalysisDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import br.com.autospec.backend.modules.vehicle.mapper.VehicleSpecMapper;
import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleAnalysisService {

    private final VehicleSpecRepository vehicleSpecRepository;
    private final VehicleSpecMapper vehicleSpecMapper;

    @Transactional(readOnly = true)
    public VehicleAnalysisDTO analyze(Long vehicleId) {
        VehicleSpec spec = vehicleSpecRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        List<VehicleSpec> allSpecs = vehicleSpecRepository.findAll();

        VehicleResponseDTO dto = vehicleSpecMapper.toResponse(spec);

        BigDecimal powerToWeight = computePowerToWeight(spec);
        BigDecimal trackHandling = computeTrackHandlingScore(spec);
        int hpPercentile  = computePercentile(allSpecs, spec, "hp");
        int tsPercentile  = computePercentile(allSpecs, spec, "topSpeed");
        int accPercentile = computePercentile(allSpecs, spec, "acceleration");

        return new VehicleAnalysisDTO(dto, powerToWeight, trackHandling, hpPercentile, tsPercentile, accPercentile);
    }

    private BigDecimal computePowerToWeight(VehicleSpec spec) {
        if (spec.getHorsepower() == null || spec.getWeight() == null || spec.getWeight().compareTo(BigDecimal.ZERO) == 0)
            return null;
        return spec.getWeight()
                .divide(BigDecimal.valueOf(spec.getHorsepower()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeTrackHandlingScore(VehicleSpec spec) {
        if (spec.getWeight() == null || spec.getHorsepower() == null) return null;
        BigDecimal ptw = computePowerToWeight(spec);
        if (ptw == null) return null;
        BigDecimal accelBonus = spec.getAcceleration() != null
                ? BigDecimal.TEN.divide(spec.getAcceleration(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return ptw.multiply(accelBonus).setScale(2, RoundingMode.HALF_UP);
    }

    private int computePercentile(List<VehicleSpec> all, VehicleSpec target, String field) {
        if (all.size() <= 1) return 50;
        long count = switch (field) {
            case "hp"           -> all.stream().filter(s -> s.getHorsepower() != null && target.getHorsepower() != null && s.getHorsepower() < target.getHorsepower()).count();
            case "topSpeed"     -> all.stream().filter(s -> s.getTopSpeed()    != null && target.getTopSpeed()    != null && s.getTopSpeed()    < target.getTopSpeed()   ).count();
            case "acceleration" -> all.stream().filter(s -> s.getAcceleration() != null && target.getAcceleration() != null && s.getAcceleration().compareTo(target.getAcceleration()) > 0).count();
            default -> 0;
        };
        return (int) Math.round((double) count / (all.size() - 1) * 100);
    }
}
