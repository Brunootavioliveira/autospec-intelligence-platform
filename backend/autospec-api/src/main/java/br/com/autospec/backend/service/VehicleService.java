package br.com.autospec.backend.service;

import br.com.autospec.backend.dto.VehicleRequestDTO;
import br.com.autospec.backend.dto.VehicleResponseDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@Service
public class VehicleService {

    public String getVehicleInfo() {
        return "Vehicle service working";
    }

    public VehicleResponseDTO generateVehicleSpec(VehicleRequestDTO request) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:5000/specs";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); //“estou enviando JSON”

        HttpEntity<VehicleRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<VehicleResponseDTO> response = restTemplate.postForEntity(
                url,
                entity,
                VehicleResponseDTO.class
        );

        return response.getBody();
    }
}
