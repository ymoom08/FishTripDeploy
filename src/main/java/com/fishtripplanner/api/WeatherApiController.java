package com.fishtripplanner.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    // ✅ 인코딩된 인증키 사용!
    private static final String SERVICE_KEY = "pjCLfIkR3vERzSAJHVhHbkUSfIRyEOyujcJxVvYnoNWPBMgzbzBxFy6tPqJyX2P2jH1n5tYeByaSGQq13oO%2F2w%3D%3D";
    private static final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getWeather(@RequestParam("region") String region) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("region", region);

        GridInfo grid = getGrid(region);
        if (grid == null) {
            result.put("error", "해당 지역의 격자 정보가 없습니다.");
            return result;
        }

        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getClosestBaseTime();

        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", grid.nx)
                .queryParam("ny", grid.ny)
                .build(true)
                .toUri();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Referer", "https://www.data.go.kr/");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            String body = response.getBody();

            if (body == null || body.isBlank() || body.trim().startsWith("<")) {
                result.put("error", "API 응답이 JSON이 아니라 HTML입니다. 인증키 또는 요청 파라미터를 확인하세요.");
                return result;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode items = mapper.readTree(body).path("response").path("body").path("items").path("item");

            if (!items.isArray()) {
                result.put("error", "예보 데이터가 없습니다.");
                return result;
            }

            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String value = item.path("fcstValue").asText();
                switch (category) {
                    case "TMP" -> result.put("temperature", value);
                    case "WSD" -> result.put("windSpeed", value);
                    case "SKY" -> result.put("sky", convertSky(value));
                    case "PTY" -> result.put("precipType", convertPty(value));
                    case "PCP" -> result.put("precipitation", value);
                    case "REH" -> result.put("humidity", value);
                }
            }

            result.put("observedAt", baseDate + " " + baseTime);

        } catch (Exception e) {
            e.printStackTrace(); // ✅ 예외 콘솔 출력 추가
            result.put("error", "예외 발생: " + e.getMessage());
        }

        return result;
    }

    private String getClosestBaseTime() {
        int[] possibleTimes = {2300, 2000, 1700, 1400, 1100, 800, 500, 200};
        int now = LocalTime.now().getHour() * 100 + LocalTime.now().getMinute();
        for (int time : possibleTimes) {
            if (now >= time) return String.format("%04d", time);
        }
        return "2300";
    }

    private String convertSky(String code) {
        return switch (code) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> "-";
        };
    }

    private String convertPty(String code) {
        return switch (code) {
            case "0" -> "없음";
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "5" -> "빗방울";
            default -> "-";
        };
    }

    // ✅ 누락된 지역까지 보완한 전체 getGrid
    private GridInfo getGrid(String region) {
        return switch (region) {
            case "서해북부" -> new GridInfo(55, 127);
            case "서해중부" -> new GridInfo(57, 126);
            case "서해남부" -> new GridInfo(51, 111);
            case "남해서부" -> new GridInfo(58, 99);
            case "제주도"   -> new GridInfo(52, 38);
            case "남해동부" -> new GridInfo(67, 99);
            case "동해남부" -> new GridInfo(98, 76);
            case "동해중부" -> new GridInfo(92, 131);
            case "동해북부" -> new GridInfo(101, 134); // ✅ 추가
            default -> null;
        };
    }

    private record GridInfo(int nx, int ny) {}
}
