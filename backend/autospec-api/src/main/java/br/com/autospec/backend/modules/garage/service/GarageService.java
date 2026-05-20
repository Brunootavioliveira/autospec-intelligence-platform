package br.com.autospec.backend.modules.garage.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.garage.dto.*;
import br.com.autospec.backend.modules.garage.entity.FleetType;
import br.com.autospec.backend.modules.garage.entity.GarageVehicle;
import br.com.autospec.backend.modules.garage.repository.GarageVehicleRepository;
import br.com.autospec.backend.modules.history.entity.ActionType;
import br.com.autospec.backend.modules.history.service.UserHistoryService;
import br.com.autospec.backend.modules.user.entity.User;
import br.com.autospec.backend.modules.vehicle.dto.VehicleResponseDTO;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import br.com.autospec.backend.modules.vehicle.mapper.VehicleSpecMapper;
import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GarageService {

    private final GarageVehicleRepository garageRepository;
    private final VehicleSpecRepository   vehicleSpecRepository;
    private final VehicleSpecMapper       vehicleSpecMapper;
    private final UserHistoryService      historyService;

    @Transactional
    public GarageVehicleResponseDTO addVehicle(User user, AddVehicleToGarageRequestDTO request) {
        if (garageRepository.existsByUserAndVehicleSpecIdAndActiveTrue(user, request.vehicleSpecId())) {
            throw new BusinessException("Veículo já está na garage");
        }

        VehicleSpec spec = vehicleSpecRepository.findById(request.vehicleSpecId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        GarageVehicle vehicle = GarageVehicle.builder()
                .user(user)
                .vehicleSpec(spec)
                .fleetType(request.fleetType())
                .nickname(request.nickname())
                .active(true)
                .build();

        GarageVehicleResponseDTO result = toDTO(garageRepository.save(vehicle));

        historyService.record(
                user,
                ActionType.SERVICE_RECORD,
                "Vehicle Added: " + spec.getBrand() + " " + spec.getModel() + " (" + spec.getYear() + ")",
                "Fleet: " + request.fleetType().name()
                        + (request.nickname() != null ? " | Nickname: " + request.nickname() : ""),
                spec.getId()
        );

        return result;
    }

    @Transactional(readOnly = true)
    public List<GarageVehicleResponseDTO> listAll(User user) {
        return garageRepository.findByUserAndActiveTrue(user)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<GarageVehicleResponseDTO> listByFleet(User user, FleetType fleetType) {
        return garageRepository.findByUserAndFleetTypeAndActiveTrue(user, fleetType)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public GarageInsightsDTO getInsights(User user) {
        List<GarageVehicle> all = garageRepository.findByUserAndActiveTrue(user);

        long activeFleetCount = all.stream()
                .filter(v -> v.getFleetType() == FleetType.PERSONAL || v.getFleetType() == FleetType.WORK)
                .count();

        String mostPowerful = all.stream()
                .filter(v -> v.getVehicleSpec().getHorsepower() != null)
                .max(Comparator.comparingInt(v -> v.getVehicleSpec().getHorsepower()))
                .map(v -> v.getVehicleSpec().getBrand() + " " + v.getVehicleSpec().getModel())
                .orElse("N/A");

        return new GarageInsightsDTO(all.size(), (int) activeFleetCount, mostPowerful);
    }

    @Transactional
    public GarageVehicleResponseDTO updateVehicle(User user, Long garageVehicleId,
                                                  UpdateGarageVehicleRequestDTO request) {
        GarageVehicle vehicle = garageRepository.findByIdAndUser(garageVehicleId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo da garage não encontrado"));

        if (request.fleetType() != null) vehicle.setFleetType(request.fleetType());
        if (request.nickname()  != null) vehicle.setNickname(request.nickname());

        return toDTO(garageRepository.save(vehicle));
    }

    @Transactional
    public void removeVehicle(User user, Long garageVehicleId) {
        GarageVehicle vehicle = garageRepository.findByIdAndUser(garageVehicleId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo da garage não encontrado"));

        vehicle.setActive(false);
        garageRepository.save(vehicle);
    }

    private GarageVehicleResponseDTO toDTO(GarageVehicle gv) {
        VehicleResponseDTO specDTO = vehicleSpecMapper.toResponse(gv.getVehicleSpec());
        return new GarageVehicleResponseDTO(
                gv.getId(),
                specDTO,
                gv.getFleetType(),
                gv.getNickname(),
                gv.isActive(),
                gv.getCreatedAt()
        );
    }
}
