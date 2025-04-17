package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 예약 요청 응답 DTO
@Getter
@Builder
public class ReservationResponseDto {
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDate reservedDate;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public static ReservationResponseDto from(ReservationRequest request) {
        return ReservationResponseDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .postId(request.getReservationPost().getId())
                .reservedDate(request.getReservedDate())
                .message(request.getMessage())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
