package br.com.autospec.backend.mapper;
import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.entity.VehicleSpec;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleSpecMapper {
    @Mapping(source = "request.brand", target = "brand")
    @Mapping(source = "request.model", target = "model")
    @Mapping(source = "request.version", target = "version")
    @Mapping(source = "request.year", target = "year")
    @Mapping(source = "response.engine", target = "engine")
    @Mapping(source = "response.horsepower", target = "horsepower")
    @Mapping(source = "response.torque", target = "torque")
    @Mapping(source = "response.drivetrain", target = "drivetrain")
    @Mapping(source = "response.topSpeed", target = "topSpeed")
    @Mapping(source = "response.acceleration", target = "acceleration")
    @Mapping(source = "response.length", target = "length")
    @Mapping(source = "response.width", target = "width")
    @Mapping(source = "response.height", target = "height")
    @Mapping(source = "response.weight", target = "weight")
    @Mapping(source = "response.electricRange", target = "electricRange")
    @Mapping(source = "response.price", target = "price")
    VehicleSpec toEntity(VehicleRequestDTO dto, VehicleResponseDTO response);
    VehicleResponseDTO toResponse(VehicleSpec vehicleSpec);
}
