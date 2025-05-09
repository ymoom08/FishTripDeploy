package com.fishtripplanner.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RouteApiController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KAKAO_API_URL = "https://apis-navi.kakaomobility.com/v1/directions";
    private static final String KAKAO_REST_API_KEY = "KakaoAK 2aed1eb5d8cab77b823ded86af7bc51d";

    @GetMapping("/route")
    public ResponseEntity<?> getRoute(@RequestParam(name = "startX") double startX,
                                      @RequestParam(name = "startY") double startY,
                                      @RequestParam(name = "endX") double endX,
                                      @RequestParam(name = "endY") double endY,
                                      @RequestParam(name = "waypoints", required = false) String waypoints) {
        try {
            StringBuilder urlBuilder = new StringBuilder(KAKAO_API_URL)
                    .append("?origin=").append(startX).append(",").append(startY)
                    .append("&destination=").append(endX).append(",").append(endY);

            if (waypoints != null && !waypoints.isBlank()) {
                urlBuilder.append("&waypoints=").append(waypoints);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", KAKAO_REST_API_KEY);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    urlBuilder.toString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode routes = root.path("routes");

            if (!routes.isArray() || routes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("경로가 없습니다.");
            }

            JsonNode firstRoute = routes.get(0);
            JsonNode summary = firstRoute.path("summary");
            // 거리 누적용 변수 추가
            int highwayDist = 0;
            int generalDist = 0;

            List<List<Double>> path = new ArrayList<>();
            for (JsonNode section : firstRoute.path("sections")) {
                for (JsonNode road : section.path("roads")) {

                    int distance = road.path("distance").asInt(0);
                    int type = road.path("type").asInt(-1);

                    if (type == 3) {
                        highwayDist += distance;
                    } else {
                        generalDist += distance;
                    }

                    JsonNode vertex = road.path("vertexes");
                    for (int i = 0; i < vertex.size(); i += 2) {
                        double x = vertex.get(i).asDouble();
                        double y = vertex.get(i + 1).asDouble();
                        path.add(List.of(x, y));
                    }
                }
            }
            //톨게이트비용!
            int toll = summary.path("fare").path("toll").asInt(0);
            //여기는 고속도로,일반도로 비율계산 섹션이예유
            int totalDist = highwayDist + generalDist;
            double highwayRatio = totalDist > 0 ? (double) highwayDist / totalDist : 0;
            double generalRatio = totalDist > 0 ? (double) generalDist / totalDist : 0;

            Map<String, Object> result = new HashMap<>();
            result.put("distance", summary.path("distance").asInt());
            result.put("duration", summary.path("duration").asInt());
            result.put("path", path);
            result.put("routes", List.of(Map.of("path", path))); // 프론트 호환용
            result.put("toll", toll);
            // 🚀 고속도로/일반도로 비율 정보 추가(연비/유류비 계산용)
            result.put("highwayDistance", highwayDist);
            result.put("generalDistance", generalDist);
            result.put("highwayRatio", highwayRatio);
            result.put("generalRatio", generalRatio);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("경로 조회 중 오류: " + e.getMessage());
        }
    }
}
