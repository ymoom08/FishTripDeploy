package com.fishtripplanner.api.khoa;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FishingIndexService {

    private static final String SERVICE_KEY = "r5iO3DlkKWJOveWMNt22HQ==";
    private static final String BASE_URL = "https://www.khoa.go.kr/api/life/point/fishing.do";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestTemplate restTemplate = new RestTemplate();

    public List<FishingIndex> getFishingIndex(String areaName, LocalDate targetDate) {
        try {
            String url = BASE_URL +
                    "?ServiceKey=" + SERVICE_KEY +
                    "&resultType=json" +
                    "&areaName=" + areaName +
                    "&base_date=" + FORMATTER.format(targetDate);

            FishingIndexResponse response = restTemplate.getForObject(url, FishingIndexResponse.class);
            return response.getResult().getData();
        } catch (Exception e) {
            log.error("생활해양예보지수(갯바위낚시) 조회 실패: area={}, date={}", areaName, targetDate, e);
            return List.of();
        }
    }

    public List<FishingIndex> filterByFishType(List<FishingIndex> list, String fishType) {
        return list.stream()
                .filter(item -> item.getFishType() != null && item.getFishType().toLowerCase(Locale.ROOT).contains(fishType.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
    }

    public int mapFishingIndexToScore(String fishingIndex) {
        if (fishingIndex == null) return 0;
        return switch (fishingIndex.trim()) {
            case "매우좋음" -> 5;
            case "좋음" -> 4;
            case "보통" -> 3;
            case "나쁨" -> 2;
            case "매우나쁨" -> 1;
            default -> 0;
        };
    }

    public Optional<FishingIndex> recommendBestTime(List<FishingIndex> list, String fishType) {
        return filterByFishType(list, fishType).stream()
                .max(Comparator.comparingInt(i -> mapFishingIndexToScore(i.getFishingIndex())));
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FishingIndexResponse {
        private Result result;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<FishingIndex> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FishingIndex {
        private String date;
        private String time;
        private String fishType;
        private String waveHeight;
        private String waterTemp;
        private String airTemp;
        private String currentSpeed;
        private String tide;

        @JsonProperty("fishingIndex")
        private String fishingIndex;
    }
}

