package com.fishtripplanner.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ReservationPostRequest {
    private Long ownerId; // 예약글 작성자 ID (사장)
    private String type; // 예약 종류 (선상, 갯바위 등)
    private String title;
    private String content;
    private String region;
    private List<LocalDate> availableDates;
    private int price;
    private String imageUrl;
}
