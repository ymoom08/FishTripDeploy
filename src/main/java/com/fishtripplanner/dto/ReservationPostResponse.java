package com.fishtripplanner.dto;

import com.fishtripplanner.domain.reservation.ReservationPost;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// 예약글 응답 DTO
@Getter
@Builder
public class ReservationPostResponse {
    private Long id;
    private String title;
    private String content;
    private String region;
    private String type;
    private List<LocalDate> availableDates;
    private int price;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static ReservationPostResponse from(ReservationPost post) {
        return ReservationPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .region(post.getRegion())
                .type(post.getType().name())
                .availableDates(post.getAvailableDates())
                .price(post.getPrice())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
