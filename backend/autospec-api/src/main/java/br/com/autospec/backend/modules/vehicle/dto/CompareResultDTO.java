package br.com.autospec.backend.modules.vehicle.dto;

public record CompareResultDTO(ComparisonWinner winner,

                               int scoreA,
                               int scoreB,

                               AttributeComparisonDTO horsepower,
                               AttributeComparisonDTO torque,
                               AttributeComparisonDTO topSpeed,
                               AttributeComparisonDTO acceleration,
                               AttributeComparisonDTO price,

                               AttributeComparisonDTO weight,
                               AttributeComparisonDTO electricRange,

                               AttributeComparisonDTO length,
                               AttributeComparisonDTO width,
                               AttributeComparisonDTO height) {
}
