package com.fishtripplanner.api;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherApiController {

    private static final String API_KEY = "mXxsZAN-QMm8bGQDflDJeQ"; // 인코딩된 인증키 그대로!
    private static final String BASE_URL = "https://apihub.kma.go.kr/api/typ01/url/kma_buoy2.php";
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public Map<String, Object> getWeather(@RequestParam("region") String region) {
        Map<String, Object> result = new HashMap<>();
        result.put("region", region);

        int stn = getStationCode(region);
        if (stn == -1) {
            result.put("error", "유효하지 않은 지역입니다.");
            return result;
        }

        String tm = LocalDateTime.now().minusMinutes(30).format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String observedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        result.put("observedAt", observedAt);

        String url = String.format("%s?tm=%s&stn=%d&authKey=%s&help=0", BASE_URL, tm, stn, API_KEY);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/plain");

        try {
            System.out.println("🌐 요청 URL: " + url);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("✅ 응답 상태코드: " + response.getStatusCode());
            System.out.println("📦 응답 내용:\n" + response.getBody());

            String body = response.getBody();

            if (body == null || body.isEmpty()) {
                result.put("error", "기상청 응답이 없습니다.");
                return result;
            }

            for (String line : body.split("\\n")) {
                if (!line.matches("^\\d{12},.*")) continue;

                String[] tokens = line.split(",");
                if (tokens.length < 14) {
                    System.out.println("⚠️ 누락된 데이터 라인 감지: " + line);
                    continue;
                }

                result.put("windSpeed", getSafeToken(tokens, 3));
                result.put("windGust", getSafeToken(tokens, 4));
                result.put("temperature", getSafeToken(tokens, 10));
                result.put("waterTemp", getSafeToken(tokens, 11));
                result.put("waveHeight", getSafeToken(tokens, 13));

                // 안전망: 값이 없으면 기본 메시지
                result.putIfAbsent("windSpeed", "데이터 없음");
                result.putIfAbsent("windGust", "데이터 없음");
                result.putIfAbsent("temperature", "데이터 없음");
                result.putIfAbsent("waterTemp", "데이터 없음");
                result.putIfAbsent("waveHeight", "데이터 없음");

                return result;
            }

            result.put("error", "관측 데이터를 찾을 수 없습니다.");
        } catch (Exception e) {
            System.out.println("❗ 예외 발생 요청 URL: " + url);
            e.printStackTrace();
            result.put("error", "기상청 API 오류: " + e.getMessage());
        }

        return result;
    }

    private int getStationCode(String region) {
        return switch (region) {
            case "서해북부" -> 22101;
            case "서해중부" -> 22102;
            case "서해남부" -> 22103;
            case "남해서부" -> 22104;
            case "제주도"   -> 22105;
            case "남해동부" -> 22106;
            case "동해남부" -> 22107;
            case "동해중부" -> 22108;
            default -> -1;
        };
    }

    private Object getSafeToken(String[] tokens, int index) {
        if (index < tokens.length) {
            try {
                String raw = tokens[index].trim();
                if (raw.isEmpty() || raw.equals("-99")) return "데이터 없음";
                double val = Double.parseDouble(raw);
                return val;
            } catch (NumberFormatException ignored) {
                return "데이터 없음";
            }
        }
        return "데이터 없음";
    }
}
