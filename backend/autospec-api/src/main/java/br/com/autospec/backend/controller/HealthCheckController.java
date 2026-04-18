package br.com.autospec.backend.controller;

import br.com.autospec.backend.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {

    private final VehicleService vehicleService;

    @GetMapping("/health-check")
    public String healthCheck() {
        return "API is running";
    }

    @GetMapping("/test-service")
    public String testService() {
        return vehicleService.getVehicleInfo();
    }
}
