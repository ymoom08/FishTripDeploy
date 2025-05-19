package com.fishtripplanner.api.khoa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripMarineInfoService {

    private final KhoaStationService khoaStationService;
    private final FishingIndexService fishingIndexService;
    private final TideForecastService tideForecastService;

    public MarineInfoResult getMarineInfo(double lat, double lon, String areaName, LocalDate targetDate) {
        MarineInfoResult result = new MarineInfoResult();

        // 1. 낚시 지수 정보
        result.setFishingIndexList(
                fishingIndexService.getFishingIndex(areaName, targetDate)
        );

        // 2. 실시간 해양관측 정보
        List<String> requiredTypes = List.of("수온", "풍속", "풍향", "기온");
        Map<String, Optional<KhoaStationService.Station>> stationMap =
                khoaStationService.findNearestStationsForDataTypes(lat, lon, requiredTypes);

        stationMap.forEach((type, stationOpt) -> stationOpt.ifPresent(s -> result.addStation(type, s)));

        // 3. 조석 예보 정보 (수온 관측소 기준)
        Optional<KhoaStationService.Station> tideStationOpt = stationMap.getOrDefault("수온", Optional.empty());
        tideStationOpt.ifPresent(station -> {
            result.setTideForecastList(
                    tideForecastService.getTideForecast(station.getObsPostId(), targetDate)
            );
        });

        return result;
    }

    @lombok.Data
    public static class MarineInfoResult {
        private List<FishingIndexService.FishingIndex> fishingIndexList;
        private Map<String, KhoaStationService.Station> stationByType;
        private List<TideForecastService.TideForecast> tideForecastList;

        public void addStation(String type, KhoaStationService.Station station) {
            if (stationByType == null) {
                stationByType = new java.util.HashMap<>();
            }
            stationByType.put(type, station);
        }
    }
}
