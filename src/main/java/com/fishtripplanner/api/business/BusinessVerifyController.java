//package com.fishtripplanner.api.business;
//
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/business")
//public class BusinessVerifyController {
//
//    @PostMapping("/verify-business")
//    public ResponseEntity<?> verifyBusiness(@RequestBody Map<String, String> request) {
//        String biznum = request.get("biznum");
//
//        String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=RdCpukPIEBtZqMxutrF20G73pIGwmOopRA9f8BkQwi%2BC7PLM60M7rYk%2FJg0OqBkGh%2FCeQsMrNWVaAXeNzgAeyw%3D%3D";
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> data = new HashMap<>();
//        List<Map<String, String>> b_no = new ArrayList<>();
//        b_no.add(Map.of("b_no", biznum));
//        data.put("b_no", b_no);
//
//        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//            return ResponseEntity.ok(response.getBody());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패: " + e.getMessage());
//        }
//    }
//}
//시큐리티 설정 추가후 구현할 부분.
