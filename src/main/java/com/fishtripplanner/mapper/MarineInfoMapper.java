package com.fishtripplanner.mapper;

import com.fishtripplanner.dto.MarineInfoResponseDto;
import com.fishtripplanner.api.khoa.FishingIndexService;
import com.fishtripplanner.api.khoa.KhoaStationService;
import com.fishtripplanner.api.khoa.TideForecastService;
import com.fishtripplanner.api.khoa.TripMarineInfoService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MarineInfoMapper {

    public static MarineInfoResponseDto toResponseDto(
            TripMarineInfoService.MarineInfoResult result,
            Optional<FishingIndexService.FishingIndex> recommended
    ) {
        return MarineInfoResponseDto.builder()
                .fishingForecast(
                        result.getFishingIndexList().stream().map(f -> MarineInfoResponseDto.FishingIndexDto.builder()
                                .date(f.getDate())
                                .time(f.getTime())
                                .fishType(f.getFishType())
                                .waveHeight(f.getWaveHeight())
                                .waterTemp(f.getWaterTemp())
                                .airTemp(f.getAirTemp())
                                .currentSpeed(f.getCurrentSpeed())
                                .tide(f.getTide())
                                .fishingIndex(f.getFishingIndex())
                                .fishingScore(new FishingIndexService().mapFishingIndexToScore(f.getFishingIndex()))
                                .build()
                        ).collect(Collectors.toList())
                )
                .tideForecast(
                        result.getTideForecastList().stream().map(t -> MarineInfoResponseDto.TideForecastDto.builder()
                                .recordTime(t.getRecordTime())
                                .tideCode(t.getTideCode())
                                .tideLevel(t.getTideLevel())
                                .build()
                        ).collect(Collectors.toList())
                )
                .observation(
                        result.getStationByType().entrySet().stream().collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> MarineInfoResponseDto.MarineStationDto.builder()
                                        .stationName(e.getValue().getObsPostName())
                                        .stationId(e.getValue().getObsPostId())
                                        .lat(e.getValue().getObsLat())
                                        .lon(e.getValue().getObsLon())
                                        .dataType(e.getValue().getDataType())
                                        .build()
                        ))
                )
                .recommendedTime(
                        recommended.map(f -> MarineInfoResponseDto.FishingIndexDto.builder()
                                .date(f.getDate())
                                .time(f.getTime())
                                .fishType(f.getFishType())
                                .waveHeight(f.getWaveHeight())
                                .waterTemp(f.getWaterTemp())
                                .airTemp(f.getAirTemp())
                                .currentSpeed(f.getCurrentSpeed())
                                .tide(f.getTide())
                                .fishingIndex(f.getFishingIndex())
                                .fishingScore(new FishingIndexService().mapFishingIndexToScore(f.getFishingIndex()))
                                .build()
                        ).orElse(null)
                )
                .build();
    }
}
