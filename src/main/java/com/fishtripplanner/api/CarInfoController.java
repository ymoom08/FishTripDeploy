package com.fishtripplanner.api;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/car")
@RequiredArgsConstructor
public class CarInfoController {

    private List<String[]> loadCsv() throws Exception {
        var resource = new ClassPathResource("static/csv/FuelInfo.csv"); // 파일명은 변경했을 경우 여기도 반영
        try (var reader = new CSVReader(new InputStreamReader(resource.getInputStream(), "EUC-KR"))) {
            return reader.readAll();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<String>> search(@RequestParam String keyword) throws Exception {
        var rows = loadCsv();
        List<String> results = rows.stream()
                .skip(1)
                .filter(r -> r.length > 4)
                .map(r -> r[0].trim() + " " + r[1].trim()) // 브랜드 + 모델명
                .filter(fullName -> fullName.toLowerCase().contains(keyword.toLowerCase()))
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }


    @GetMapping("/model")
    public ResponseEntity<Map<String, Object>> getModel(@RequestParam String name) throws Exception {
        var rows = loadCsv();
        for (String[] r : rows) {
            if (r.length > 4 && (r[0] + " " + r[1]).equalsIgnoreCase(name)) {
                Map<String, Object> result = new HashMap<>();
                result.put("fuelType", r[2].trim());
                result.put("cityEff", Double.parseDouble(r[3].trim()));
                result.put("highwayEff", Double.parseDouble(r[4].trim()));
                return ResponseEntity.ok(result);
            }
        }
        return ResponseEntity.notFound().build();
    }


    private double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}



