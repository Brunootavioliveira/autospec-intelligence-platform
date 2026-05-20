package br.com.autospec.backend.modules.garage.dto;

public record GarageInsightsDTO(
        int totalVehicles,
        int activeFleet,
        String mostPowerful
) {
}
