package com.fishtripplanner.dto.party;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCreateRequest {
    private Long leaderId;             // 파티장 ID
    private String title;              // 제목
    private String description;        // 상세 설명
    private String region;             // 지역명
    private String departurePoint;     // 출발지 명칭
    private Double departureLat;       // 출발지 위도
    private Double departureLng;       // 출발지 경도
    private String destination;        // 목적지 명칭
    private LocalDateTime departureTime; // 출발 시간
    private int maxParticipants;       // 최대 인원
    private int estimatedCost;         // 예상 비용
    private List<String> stopovers;    // 경유지 리스트
    private List<Integer> stopoverStayTimes; // 각 경유지 체류 시간(분)
    private Long reservationPostId;//만약 예약이 포함된다면 들어갈 정보 default 를 설정해주어야 null이 안뜰 것 . 이건 선택임.
}