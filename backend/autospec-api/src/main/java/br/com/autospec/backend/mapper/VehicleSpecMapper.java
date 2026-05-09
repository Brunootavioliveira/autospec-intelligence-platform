package br.com.autospec.backend.mapper;
import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.entity.VehicleSpec;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleSpecMapper {
    VehicleSpec toEntity(VehicleRequestDTO dto);
    VehicleResponseDTO toResponse(VehicleSpec vehicleSpec);
}
