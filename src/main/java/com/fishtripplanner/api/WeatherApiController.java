package com.fishtripplanner.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    @GetMapping
    public Map<String, Object> getWeather(@RequestParam("region") String region) {
        Map<String, Object> result = new HashMap<>();
        result.put("region", region);

        try {
            // ✅ 인코딩된 인증키 (절대 URLEncoder.encode 하지 말 것)
            String encodedServiceKey = "fC6erys%2BXLk%2FSOHz1GQd9zeEOEPD7K3SRme35BKMb2dPw2T4dE%2FaclltkMMJEGPx1KXX1QCVWebuTQ7Fu%2FRHDA%3D%3D";

            // ✅ 지역 → 기관코드 + 지점코드 매핑
            RegionInfo info = getRegionInfo(region);
            if (info == null) throw new RuntimeException("유효하지 않은 지역입니다.");

            // ✅ API 요청 URL 구성 (인코딩된 키 그대로 사용 → build(true))
            String url = UriComponentsBuilder
                    .fromHttpUrl("http://marineweather.nmpnt.go.kr:8001/openWeatherNow.do")
                    .queryParam("serviceKey", encodedServiceKey)
                    .queryParam("resultType", "json")
                    .queryParam("mmaf", info.mmaf)
                    .queryParam("mmsi", info.mmsi)
                    .queryParam("dataType", "1")
                    .build(true) // 🔥 true 필수
                    .toUriString();

            System.out.println("[DEBUG] 호출 URL: " + url);

            // ✅ HTTP 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String body = response.getBody();

            System.out.println("[DEBUG] 원본 응답: " + body);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            JsonNode recordset = root.path("result").path("recordset");

            if (recordset.isArray() && recordset.size() > 0) {
                JsonNode data = recordset.get(0);
                result.put("datetime", data.path("DATETIME").asText());
                result.put("temperature", data.path("WATER_TEMPER").asText());
                result.put("windSpeed", data.path("WIND_SPEED").asText());
                result.put("waveHeight", data.path("WAVE_HEIGTH").asText());
                result.put("humidity", data.path("HUMIDITY").asText());
                result.put("salinity", data.path("SALINITY").asText());
            } else {
                throw new RuntimeException("관측 데이터가 없습니다.");
            }

        } catch (Exception e) {
            System.err.println("❌ API 호출 실패: " + e.getMessage());
            result.put("error", "날씨 정보를 불러오는데 실패했습니다.");
        }

        return result;
    }

    // ✅ 지역명 → 관측 정보 매핑
    private RegionInfo getRegionInfo(String region) {
        return switch (region) {
            case "서해북부" -> new RegionInfo("106", "994401042");   // 입파도등대
            case "서해중부" -> new RegionInfo("102", "994401018");   // 인천항석탄부두A호등대
            case "서해남부" -> new RegionInfo("108", "994403661");   // 군산흑도등표
            case "남해서부" -> new RegionInfo("107", "1079003");     // 가거도등대
            case "제주도"   -> new RegionInfo("112", "994403894");   // 김녕항서방파제등대
            case "남해동부" -> new RegionInfo("109", "994401623");   // 고도등표
            case "동해남부" -> new RegionInfo("110", "994403579");   // 영일만항분리항로부표
            case "동해중부" -> new RegionInfo("111", "994403800");   // 묵호등대
            case "동해북부" -> new RegionInfo("111", "994403810");   // 주문진항등대
            default -> null;
        };
    }

    // ✅ 관측소 정보 클래스
    private static class RegionInfo {
        String mmaf; // 기관코드
        String mmsi; // 지점코드
        RegionInfo(String mmaf, String mmsi) {
            this.mmaf = mmaf;
            this.mmsi = mmsi;
        }
    }
}

