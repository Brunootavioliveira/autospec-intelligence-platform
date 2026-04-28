package br.com.autospec.backend.service;

import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import br.com.autospec.backend.entity.VehicleSpec;
import br.com.autospec.backend.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleSpecRepository vehicleSpecRepository;

    public String getVehicleInfo() {
        return "Vehicle service working";
    }

    public VehicleResponseDTO generateVehicleSpec(VehicleRequestDTO request) {

        Optional<VehicleSpec> existing = vehicleSpecRepository.findByBrandAndModelAndVersionAndYear(
                request.brand(),
                request.model(),
                request.version(),
                request.year()
        );

        if (existing.isPresent()) {
            VehicleSpec spec = existing.get();

            return new VehicleResponseDTO(
                    spec.getEngine(),
                    spec.getHorsepower(),
                    spec.getTorque(),
                    spec.getDrivetrain(),
                    spec.getPrice()
            );
        }
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://ai-service:5000/specs";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); //“estou enviando JSON”

        HttpEntity<VehicleRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<VehicleResponseDTO> response = restTemplate.postForEntity(
                url,
                entity,
                VehicleResponseDTO.class
        );


        VehicleResponseDTO responseBody = response.getBody();

        if (responseBody.engine().equals("Unknown")) {
            return responseBody;
        }

        VehicleSpec vehicleSpec = new VehicleSpec();
        vehicleSpec.setBrand(request.brand());
        vehicleSpec.setModel(request.model());
        vehicleSpec.setVersion(request.version());
        vehicleSpec.setYear(request.year());

        vehicleSpec.setEngine(responseBody.engine());
        vehicleSpec.setHorsepower(responseBody.horsepower());
        vehicleSpec.setTorque(responseBody.torque());
        vehicleSpec.setDrivetrain(responseBody.drivetrain());
        vehicleSpec.setPrice(responseBody.price());


        vehicleSpecRepository.save(vehicleSpec);

        return responseBody;
    }
}
