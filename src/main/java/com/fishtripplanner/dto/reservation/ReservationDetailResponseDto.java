package com.fishtripplanner.dto.reservation;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReservationDetailResponseDto {

    private Long id;
    private String title;
    private String imageUrl;
    private String regionName;
    private String companyName;

    private String type;        // ENUM 이름
    private String typeLower;   // 소문자 변환
    private String typeKorean;  // 한글 표시

    private Integer price;
    private String content;
    private List<String> fishTypes;

    // ⬇⬇ 날짜마다 남은 자리 포함
    private List<AvailableDateDto> availableDates;

    @Getter
    @Builder
    public static class AvailableDateDto {
        private String  date;      // yyyy-MM-dd
        private String  time;      // 06:00~14:00
        private Integer capacity;  // 정원
        private Integer remaining; // ⚠️ 남은 자리 ── 여기!
    }
}
