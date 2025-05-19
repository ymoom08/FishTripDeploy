package com.fishtripplanner.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarineInfoResponseDto {

    // 낚시 예보 정보 리스트
    private List<FishingIndexDto> fishingForecast;

    // 조석 예보 리스트
    private List<TideForecastDto> tideForecast;

    // 수온/풍속/기온 등 관측소별 정보
    private Map<String, MarineStationDto> observation;

    // 추천 시간대 (특정 어종 기준)
    private FishingIndexDto recommendedTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FishingIndexDto {
        private String date;
        private String time;
        private String fishType;
        private String waveHeight;
        private String waterTemp;
        private String airTemp;
        private String currentSpeed;
        private String tide;
        private String fishingIndex;
        private int fishingScore; // 예: 매우좋음 → 5점
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TideForecastDto {
        private String recordTime;
        private String tideLevel;
        private String tideCode; // H/L
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarineStationDto {
        private String stationName;
        private String stationId;
        private double lat;
        private double lon;
        private String dataType;
    }
}
