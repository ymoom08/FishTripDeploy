package com.fishtripplanner.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FuelPriceController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_KEY = "F250509352";
    private static final String OIL_API_URL = "https://www.opinet.co.kr/api/avgAllPrice.do?code=" + API_KEY + "&out=json";

    @GetMapping("/fuel-price")
    public ResponseEntity<?> getFuelPrices() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(OIL_API_URL, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode oilList = root.path("RESULT").path("OIL");

            Map<String, Integer> prices = new HashMap<>();
            for (JsonNode oil : oilList) {
                String code = oil.path("PRODCD").asText();
                double price = oil.path("PRICE").asDouble();
                if (code.equals("B027")) prices.put("휘발유", (int) Math.round(price));
                if (code.equals("D047")) prices.put("경유", (int) Math.round(price));
                if (code.equals("C004")) prices.put("LPG", (int) Math.round(price));
            }

            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("유가 정보 조회 실패: " + e.getMessage());
        }
    }
}
