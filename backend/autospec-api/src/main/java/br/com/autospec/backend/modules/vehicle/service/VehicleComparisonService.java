package br.com.autospec.backend.modules.vehicle.service;

import br.com.autospec.backend.core.exception.BusinessException;
import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.vehicle.dto.*;
import br.com.autospec.backend.modules.vehicle.entity.VehicleSpec;
import br.com.autospec.backend.modules.vehicle.mapper.VehicleSpecMapper;
import br.com.autospec.backend.modules.vehicle.repository.VehicleSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class VehicleComparisonService {

    private final VehicleService vehicleService;
    private final VehicleSpecRepository vehicleSpecRepository;
    private final VehicleSpecMapper vehicleSpecMapper;

    @Transactional(readOnly = true)
    public VehicleCompareResponseDTO compare(Long idA, Long idB) {

        if (idA.equals(idB)) {
            throw new BusinessException(
                    "Os dois IDs devem ser de veículos diferentes"
            );
        }

        VehicleSpec vehicleA = vehicleSpecRepository.findById(idA)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Veículo A não encontrado"));

        VehicleSpec vehicleB = vehicleSpecRepository.findById(idB)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Veículo B não encontrado"));

        VehicleResponseDTO responseA = vehicleSpecMapper.toResponse(vehicleA);
        VehicleResponseDTO responseB = vehicleSpecMapper.toResponse(vehicleB);

        return buildComparison(responseA, responseB);
    }

    private VehicleCompareResponseDTO buildComparison(
            VehicleResponseDTO a,
            VehicleResponseDTO b
    ) {

        int scoreA = 0;
        int scoreB = 0;

        var horsepower = compareInteger(a.horsepower(), b.horsepower(), true);
        var torque = compareBigDecimal(a.torque(), b.torque(), true);
        var topSpeed = compareInteger(a.topSpeed(), b.topSpeed(), true);
        var acceleration = compareBigDecimal(a.acceleration(), b.acceleration(), false);

        var price = compareBigDecimal(a.price(), b.price(), false);

        var weight = compareBigDecimal(a.weight(), b.weight(), false);

        var length = compareBigDecimal(a.length(), b.length(), false);
        var width = compareBigDecimal(a.width(), b.width(), false);
        var height = compareBigDecimal(a.height(), b.height(), false);

        var electricRange = compareInteger(
                a.electricRange(),
                b.electricRange(),
                true
        );

        scoreA += pointForA(horsepower);
        scoreB += pointForB(horsepower);

        scoreA += pointForA(torque);
        scoreB += pointForB(torque);

        scoreA += pointForA(topSpeed);
        scoreB += pointForB(topSpeed);

        scoreA += pointForA(acceleration);
        scoreB += pointForB(acceleration);

        scoreA += pointForA(price);
        scoreB += pointForB(price);

        scoreA += pointForA(weight);
        scoreB += pointForB(weight);

        scoreA += pointForA(length);
        scoreB += pointForB(length);

        scoreA += pointForA(width);
        scoreB += pointForB(width);

        scoreA += pointForA(height);
        scoreB += pointForB(height);

        scoreA += pointForA(electricRange);
        scoreB += pointForB(electricRange);

        ComparisonWinner finalWinner =
                scoreA > scoreB
                        ? ComparisonWinner.VEHICLE_A
                        : scoreB > scoreA
                          ? ComparisonWinner.VEHICLE_B
                          : ComparisonWinner.DRAW;

        CompareResultDTO comparison = new CompareResultDTO(
                finalWinner,
                scoreA,
                scoreB,

                horsepower,
                torque,
                topSpeed,
                acceleration,
                price,

                weight,
                electricRange,

                length,
                width,
                height
        );

        return new VehicleCompareResponseDTO(
                a,
                b,
                comparison
        );
    }

    private int pointForA(AttributeComparisonDTO result) {
        return result.winner() == ComparisonWinner.VEHICLE_A ? 1 : 0;
    }

    private int pointForB(AttributeComparisonDTO result) {
        return result.winner() == ComparisonWinner.VEHICLE_B ? 1 : 0;
    }

    private AttributeComparisonDTO compareInteger(
            Integer valueA,
            Integer valueB,
            boolean higherIsBetter
    ) {

        String formattedA =
                valueA != null ? String.valueOf(valueA) : "N/A";

        String formattedB =
                valueB != null ? String.valueOf(valueB) : "N/A";

        if (valueA == null && valueB == null) {
            return new AttributeComparisonDTO(
                    ComparisonWinner.DRAW,
                    formattedA,
                    formattedB
            );
        }

        if (valueA == null) {
            return new AttributeComparisonDTO(
                    ComparisonWinner.VEHICLE_B,
                    formattedA,
                    formattedB
            );
        }

        if (valueB == null) {
            return new AttributeComparisonDTO(
                    ComparisonWinner.VEHICLE_A,
                    formattedA,
                    formattedB
            );
        }

        int comparison = Integer.compare(valueA, valueB);

        ComparisonWinner winner;

        if (comparison == 0) {
            winner = ComparisonWinner.DRAW;
        } else {
            winner = higherIsBetter
                    ? (comparison > 0
                       ? ComparisonWinner.VEHICLE_A
                       : ComparisonWinner.VEHICLE_B)
                    : (comparison < 0
                       ? ComparisonWinner.VEHICLE_A
                       : ComparisonWinner.VEHICLE_B);
        }

        return new AttributeComparisonDTO(
                winner,
                formattedA,
                formattedB
        );
    }

    private AttributeComparisonDTO compareBigDecimal(
            BigDecimal valueA,
            BigDecimal valueB,
            boolean higherIsBetter
    ) {

        String formattedA =
                valueA != null ? valueA.toPlainString() : "N/A";

        String formattedB =
                valueB != null ? valueB.toPlainString() : "N/A";

        if (valueA == null && valueB == null) {
            return new AttributeComparisonDTO(
                    ComparisonWinner.DRAW,
                    formattedA,
                    formattedB
            );
        }

        if (valueA == null) {
            return new AttributeComparisonDTO(
                    ComparisonWinner.VEHICLE_B,
                    formattedA,
                    formattedB
            );
        }

        if (valueB == null) {
            return new AttributeComparisonDTO(
                    ComparisonWinner.VEHICLE_A,
                    formattedA,
                    formattedB
            );
        }

        int comparison = valueA.compareTo(valueB);

        ComparisonWinner winner;

        if (comparison == 0) {
            winner = ComparisonWinner.DRAW;
        } else {
            winner = higherIsBetter
                    ? (comparison > 0
                       ? ComparisonWinner.VEHICLE_A
                       : ComparisonWinner.VEHICLE_B)
                    : (comparison < 0
                       ? ComparisonWinner.VEHICLE_A
                       : ComparisonWinner.VEHICLE_B);
        }

        return new AttributeComparisonDTO(
                winner,
                formattedA,
                formattedB
        );
    }

    @Transactional
    public VehicleCompareResponseDTO compare(VehicleCompareRequestDTO request) {

        VehicleResponseDTO vehicleA = vehicleService.generateVehicleSpec(request.vehicleA());

        VehicleResponseDTO vehicleB = vehicleService.generateVehicleSpec(request.vehicleB());

        return buildComparison(vehicleA, vehicleB);
    }
}
