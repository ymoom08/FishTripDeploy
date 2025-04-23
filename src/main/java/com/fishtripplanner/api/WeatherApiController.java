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

    // ❗ 인코딩되지 않은 원본 키 사용 (절대 encode() 하지 마세요!)
    private final String apiKey = "mXxsZAN-QMm8bGQDflDJeQ";

    @GetMapping
    public Map<String, Object> getWeather(@RequestParam("region") String region) {
        Map<String, Object> result = new HashMap<>();
        result.put("region", region);

        try {
            String tm = LocalDateTime.now().minusMinutes(30)
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            int stn = getStationCode(region);
            if (stn == -1) throw new RuntimeException("유효하지 않은 지역입니다.");

            // 요청 URL 생성
            String url = "https://apihub.kma.go.kr/api/typ01/url/kma_buoy2.php"
                    + "?tm=" + tm
                    + "&stn=" + stn
                    + "&authKey=" + apiKey
                    + "&help=1";

            System.out.println("[DEBUG] 요청 URL: " + url);

            // 요청 헤더 구성
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "text/plain");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            System.out.println("[DEBUG] 응답 본문:\n" + body);

            String[] lines = body.split("\n");
            String dataLine = null;

            for (String line : lines) {
                if (line.matches("^\\d{12},.*")) {
                    dataLine = line;
                }
            }

            if (dataLine != null) {
                String[] tokens = dataLine.split(",");

                result.put("windSpeed", tokens[3].trim());      // WS1
                result.put("windGust", tokens[4].trim());       // WS1_GST
                result.put("temperature", tokens[10].trim());   // TA
                result.put("waterTemp", tokens[11].trim());     // TW
                result.put("waveHeight", tokens[13].trim());    // WH_SIG
            } else {
                result.put("error", "관측 데이터를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "날씨 정보를 불러오는데 실패했습니다: " + e.getMessage());
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
            case "동해북부" -> 22109;
            default -> -1;
        };
    }
}
