package br.com.autospec.backend.modules.comparison.service;

import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.comparison.dto.SaveComparisonRequestDTO;
import br.com.autospec.backend.modules.comparison.dto.SavedComparisonResponseDTO;
import br.com.autospec.backend.modules.comparison.entity.SavedComparison;
import br.com.autospec.backend.modules.comparison.repository.SavedComparisonRepository;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import br.com.autospec.backend.modules.vehicle.mapper.VehicleSpecMapper;
import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedComparisonService {
    private final SavedComparisonRepository savedComparisonRepository;
    private final VehicleSpecRepository vehicleSpecRepository;
    private final VehicleSpecMapper vehicleSpecMapper;

    @Transactional
    public SavedComparisonResponseDTO save(User user, SaveComparisonRequestDTO request) {
        VehicleSpec vA = vehicleSpecRepository.findById(request.vehicleAId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo A não encontrado"));
        VehicleSpec vB = vehicleSpecRepository.findById(request.vehicleBId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo B não encontrado"));

        String title = request.title() != null ? request.title()
                : vA.getModel() + " vs. " + vB.getModel();

        SavedComparison sc = SavedComparison.builder()
                .user(user)
                .vehicleA(vA)
                .vehicleB(vB)
                .title(title)
                .build();

        return toDTO(savedComparisonRepository.save(sc));
    }

    @Transactional(readOnly = true)
    public Page<SavedComparisonResponseDTO> list(User user, Pageable pageable) {
        return savedComparisonRepository.findByUser(user, pageable).map(this::toDTO);
    }

    @Transactional
    public void delete(User user, Long id) {
        SavedComparison sc = savedComparisonRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Comparação salva não encontrada"));
        savedComparisonRepository.delete(sc);
    }

    private SavedComparisonResponseDTO toDTO(SavedComparison sc) {
        return new SavedComparisonResponseDTO(
                sc.getId(),
                sc.getTitle(),
                vehicleSpecMapper.toResponse(sc.getVehicleA()),
                vehicleSpecMapper.toResponse(sc.getVehicleB()),
                sc.getCreatedAt()
        );
    }
}
