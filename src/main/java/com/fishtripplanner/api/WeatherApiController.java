package com.fishtripplanner.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    private static final String SERVICE_KEY = "wldhxng34hkddbsgm81lwldhxng34hkddbsgm81l==";
    private static final String BASE_URL = "http://www.khoa.go.kr/api/oceangrid/buObsRecent/search.do";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiController.class);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getWeather(@RequestParam("region") String region) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("region", region);

        try {
            // 관측소 코드 가져오기
            String buoyCode = getBuoyCodeForRegion(region);
            if (buoyCode == null) {
                result.put("error", "지원하지 않는 해역입니다.");
                return result;
            }

            // API URL 구성
            String url = BASE_URL
                    + "?ServiceKey=" + SERVICE_KEY
                    + "&ObsCode=" + buoyCode
                    + "&ResultType=json";

            logger.info("API 요청 URL: {}", url); // 로그 추가

            // API 요청 및 응답 받기
            String body = restTemplate.getForObject(url, String.class);

            logger.info("API 응답: {}", body); // 로그 추가

            if (body == null || body.isBlank()) {
                result.put("error", "API 응답이 비어 있습니다.");
                return result;
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode dataArray = root.path("result").path("data");

            if (dataArray.isArray() && dataArray.size() > 0) {
                JsonNode item = dataArray.get(0);

                // 해양 날씨 데이터 추출
                Map<String, String> weatherData = new LinkedHashMap<>();
                weatherData.put("observationTime", item.path("record_time").asText());
                weatherData.put("waterTemperature", item.path("water_temp").asText() + "°C");
                weatherData.put("salinity", item.path("Salinity").asText() + " psu");
                weatherData.put("airTemperature", item.path("air_temp").asText() + "°C");
                weatherData.put("windSpeed", item.path("wind_speed").asText() + " m/s");
                weatherData.put("waveHeight", item.path("wave_height").asText() + " m");

                if (weatherData.isEmpty()) {
                    result.put("error", "해당 해역에 대한 날씨 데이터가 없습니다.");
                } else {
                    result.putAll(weatherData);
                }
            } else {
                result.put("error", "해당 해역에 대한 해양 관측 데이터가 없습니다.");
            }

        } catch (Exception e) {
            logger.error("예외 발생: ", e); // 오류 로그 추가
            result.put("error", "예외 발생: " + e.getMessage());
        }

        return result;
    }

    // 지역에 따른 국립해양조사원 부이 코드 매핑 (실제 코드는 API 문서 참고하여 구현해야 함)
    private String getBuoyCodeForRegion(String region) {
        return switch (region) {
            case "서해북부" -> "TB01";
            case "서해중부" -> "TD02";
            case "서해남부" -> "TA03";
            case "남해서부" -> "NB01";
            case "남해동부" -> "ND02";
            case "제주도" -> "JE01";
            case "동해남부" -> "EB01";
            case "동해중부" -> "ED02";
            case "동해북부" -> "EE03";
            case "해운대" -> "TW_0062";
            default -> null;
        };
    }
}