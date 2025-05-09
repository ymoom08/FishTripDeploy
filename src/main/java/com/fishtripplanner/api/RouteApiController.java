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

            List<List<Double>> path = new ArrayList<>();
            for (JsonNode section : firstRoute.path("sections")) {
                for (JsonNode road : section.path("roads")) {
                    JsonNode vertex = road.path("vertexes");
                    for (int i = 0; i < vertex.size(); i += 2) {
                        double x = vertex.get(i).asDouble();
                        double y = vertex.get(i + 1).asDouble();
                        path.add(List.of(x, y));
                    }
                }
            }
            int toll = summary.path("fare").path("toll").asInt(0);
            Map<String, Object> result = new HashMap<>();
            result.put("distance", summary.path("distance").asInt());
            result.put("duration", summary.path("duration").asInt());
            result.put("path", path);
            result.put("routes", List.of(Map.of("path", path))); // 프론트 호환용
            result.put("toll", toll);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("경로 조회 중 오류: " + e.getMessage());
        }
    }
}
