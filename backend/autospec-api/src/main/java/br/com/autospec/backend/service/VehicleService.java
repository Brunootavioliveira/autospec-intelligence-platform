package br.com.autospec.backend.service;

import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.entity.VehicleSpec;
import br.com.autospec.backend.exception.ResourceNotFoundException;
import br.com.autospec.backend.mapper.VehicleSpecMapper;
import br.com.autospec.backend.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleSpecRepository vehicleSpecRepository;
    private final VehicleSpecMapper vehicleSpecMapper;
    private final WebClient webClient;

    public String getVehicleInfo() {
        return "Vehicle service working";
    }

    @Transactional
    public VehicleResponseDTO generateVehicleSpec(VehicleRequestDTO request) {

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
                .orElseThrow(() -> new RuntimeException("AI service retornou resposta vazia"));

        if (responseBody.engine().equals("Unknown")) {
            return responseBody;
        }

        VehicleSpec vehicleSpec = vehicleSpecMapper.toEntity(request, responseBody);
        vehicleSpecRepository.save(vehicleSpec);

        return responseBody;
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO findById(Long id) {

        VehicleSpec vehicleSpec = vehicleSpecRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Veiculo não encontrado"));

        return vehicleSpecMapper.toResponse(vehicleSpec);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> findAll(Pageable pageable) {

        if (pageable.getPageSize() > 10) {
            throw new IllegalArgumentException("Máximo de 10 itens por página");
        }

        Page<VehicleSpec> vehicleSpec = vehicleSpecRepository.findAll(pageable);

        return vehicleSpec.map(vehicleSpecMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {

        VehicleSpec vehicleSpec = vehicleSpecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo não encontrado"));

        vehicleSpecRepository.delete(vehicleSpec);
    }
}
