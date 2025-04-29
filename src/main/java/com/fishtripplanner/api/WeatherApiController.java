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

    private static final String SERVICE_KEY = "TmVlowO4xB39rQw36KZ0EQ=="; // 새 인증키
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
                result.put("error", "지원하지 않는 지역입니다.");
                return result;
            }

            // API URL 구성
            String url = BASE_URL
                    + "?ServiceKey=" + SERVICE_KEY
                    + "&ObsCode=" + buoyCode
                    + "&ResultType=json";

            logger.info("API 요청 URL: {}", url);

            // API 요청 및 응답 받기
            String body = restTemplate.getForObject(url, String.class);
            logger.info("API 응답: {}", body);

            if (body == null || body.isBlank()) {
                result.put("error", "API 응답이 비어 있습니다.");
                return result;
            }

            JsonNode root = objectMapper.readTree(body);
            JsonNode dataNode = root.path("result").path("data");

            if (dataNode != null && dataNode.has("record_time")) {
                // 해양 날씨 데이터 추출
                result.put("관측시간", dataNode.path("record_time").asText());
                result.put("수온", dataNode.path("water_temp").asText() + "°C");
                result.put("염분", dataNode.path("Salinity").asText() + " psu");
                result.put("기온", dataNode.path("air_temp").asText() + "°C");
                result.put("풍속", dataNode.path("wind_speed").asText() + " m/s");
                result.put("파고", dataNode.path("wave_height").asText() + " m");
                result.put("풍향", dataNode.path("wind_dir").asText() + "°");
                result.put("유속", dataNode.path("current_speed").asText() + " cm/s");
                result.put("유향", dataNode.path("current_dir").asText() + "°");
                result.put("기압", dataNode.path("air_pres").asText() + " hPa");
            } else {
                result.put("error", "실시간 해양 관측 데이터가 없습니다.");
            }

        } catch (Exception e) {
            logger.error("예외 발생: ", e);
            result.put("error", "예외 발생: " + e.getMessage());
        }

        return result;
    }

    /**
     * 지역명에 따른 관측소 코드 매핑
     */
    private String getBuoyCodeForRegion(String region) {
        return switch (region) {
            case "감천항" -> "TW_0088";
            case "경인항" -> "TW_0077";
            case "경포대해수욕장" -> "TW_0089";
            case "고래불해수욕장" -> "TW_0095";
            case "광양항" -> "TW_0074";
            case "군산항" -> "TW_0072";
            case "낙산해수욕장" -> "TW_0091";
            case "남해동부" -> "KG_0025";
            case "대천해수욕장" -> "TW_0069";
            case "대한해협" -> "KG_0024";
            case "마산항" -> "TW_0085";
            case "망상해수욕장" -> "TW_0094";
            case "부산항" -> "TW_0087";
            case "부산항신항" -> "TW_0086";
            case "상왕등도" -> "TW_0079";
            case "생일도" -> "TW_0081";
            case "속초해수욕장" -> "TW_0093";
            case "송정해수욕장" -> "TW_0090";
            case "여수항" -> "TW_0083";
            case "완도항" -> "TW_0078";
            case "우이도" -> "TW_0080";
            case "울릉도북동" -> "KG_0101";
            case "울릉도북서" -> "KG_0102";
            case "인천항" -> "TW_0076";
            case "임랑해수욕장" -> "TW_0092";
            case "제주남부" -> "KG_0021";
            case "제주해협" -> "KG_0028";
            case "중문해수욕장" -> "TW_0075";
            case "태안항" -> "TW_0082";
            case "통영항" -> "TW_0084";
            case "평택당진항" -> "TW_0070";
            case "한수원_고리" -> "HB_0002";
            case "한수원_기장" -> "HB_0001";
            case "한수원_나곡" -> "HB_0009";
            case "한수원_덕천" -> "HB_0008";
            case "한수원_온양" -> "HB_0007";
            case "한수원_진하" -> "HB_0003";
            case "해운대해수욕장" -> "TW_0062";
            default -> null;
        };
    }
}
