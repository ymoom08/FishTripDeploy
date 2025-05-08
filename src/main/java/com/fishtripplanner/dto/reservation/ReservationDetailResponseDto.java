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
    private String type;
    private Integer price;
    private String content;
    private List<String> fishTypes;
    private List<AvailableDateDto> availableDates;

    @Getter
    @Builder
    public static class AvailableDateDto {
        private String date;       // YYYY-MM-DD
        private int remaining;     // 남은 인원 수
    }
}
