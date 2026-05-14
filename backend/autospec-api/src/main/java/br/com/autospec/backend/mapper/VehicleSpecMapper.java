package br.com.autospec.backend.mapper;
import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.entity.VehicleSpec;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleSpecMapper {
    @Mapping(source = "dto.brand", target = "brand")
    @Mapping(source = "dto.model", target = "model")
    @Mapping(source = "dto.version", target = "version")
    @Mapping(source = "dto.year", target = "year")
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

    @Mapping(source = "id", target = "id")
    @Mapping(source = "brand", target = "brand")
    @Mapping(source = "model", target = "model")
    @Mapping(source = "version", target = "version")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "engine", target = "engine")
    @Mapping(source = "horsepower", target = "horsepower")
    @Mapping(source = "torque", target = "torque")
    @Mapping(source = "drivetrain", target = "drivetrain")
    @Mapping(source = "topSpeed", target = "topSpeed")
    @Mapping(source = "acceleration", target = "acceleration")
    @Mapping(source = "length", target = "length")
    @Mapping(source = "width", target = "width")
    @Mapping(source = "height", target = "height")
    @Mapping(source = "weight", target = "weight")
    @Mapping(source = "electricRange", target = "electricRange")
    @Mapping(source = "price", target = "price")
    VehicleResponseDTO toResponse(VehicleSpec vehicleSpec);
}
