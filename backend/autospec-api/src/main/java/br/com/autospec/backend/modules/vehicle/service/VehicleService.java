package br.com.autospec.backend.modules.vehicle.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.modules.vehicle.dto.VehicleRequestDTO;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.vehicle.mapper.VehicleSpecMapper;
import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleSpecRepository vehicleSpecRepository;
    private final VehicleSpecMapper vehicleSpecMapper;
    private final WebClient webClient;

    @Cacheable(value = "vehicle-specs-by-key",
            key = "#request.brand() + '-' + #request.model() + '-' + #request.version() + '-' + #request.year()")
    public VehicleResponseDTO generateVehicleSpec(VehicleRequestDTO request) {
        return doGenerateVehicleSpec(request);
    }

    @Transactional
    public VehicleResponseDTO doGenerateVehicleSpec(VehicleRequestDTO request) {

        int currentYear = LocalDate.now().getYear();

        if (request.year() > currentYear + 1) {
            throw new BusinessException("O ano do veículo não pode ser no futuro");
        }

        Optional<VehicleSpec> existing = vehicleSpecRepository.findByBrandAndModelAndVersionAndYear(
                request.brand(),
                request.model(),
                request.version(),
                request.year()
        );

        if (existing.isPresent()) {
            return vehicleSpecMapper.toResponse(existing.get());
        }

        VehicleResponseDTO responseBody = webClient
                .post()
                .uri("/specs")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VehicleResponseDTO.class)
                .blockOptional()
                .orElseThrow(() -> new BusinessException("Não foi possível gerar as especificações técnicas no momento."));

        if (responseBody.engine().equals("Unknown")) {
            return responseBody;
        }

        VehicleSpec vehicleSpec = vehicleSpecMapper.toEntity(request, responseBody);
        VehicleSpec savedVehicle = vehicleSpecRepository.save(vehicleSpec);

        return vehicleSpecMapper.toResponse(savedVehicle);
    }

    @Cacheable(value = "vehicle-specs-by-id", key = "#id")
    @Transactional(readOnly = true)
    public VehicleResponseDTO findById(Long id) {

        VehicleSpec vehicleSpec = vehicleSpecRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Veiculo não encontrado"));

        return vehicleSpecMapper.toResponse(vehicleSpec);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> findAll(Pageable pageable) {

        if (pageable.getPageSize() > 10) {
            throw new BusinessException("Máximo de 10 itens por página");
        }

        Page<VehicleSpec> vehicleSpec = vehicleSpecRepository.findAll(pageable);

        return vehicleSpec.map(vehicleSpecMapper::toResponse);
    }

    @Cacheable(value = "vehicle-specs-by-id", key = "#id")
    @Transactional
    public void delete(Long id) {

        VehicleSpec vehicleSpec = vehicleSpecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo não encontrado"));

        vehicleSpecRepository.delete(vehicleSpec);
    }
}
