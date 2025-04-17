package com.fishtripplanner.dto.reservation;

import lombok.Getter;

import java.time.LocalDate;

// 예약 요청 요청 DTO
@Getter
public class CreateReservationRequestDto {
    private Long userId; // 예약 신청자 ID
    private Long postId; // 대상 예약글 ID
    private LocalDate reservedDate;
    private String message; // 전달 메세지
}