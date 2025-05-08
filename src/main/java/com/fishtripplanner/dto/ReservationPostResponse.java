package com.fishtripplanner.dto;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationPostAvailableDate;
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
    private String region; // 여기에 지역 이름(String)을 담아야 하니까...
    private String type;
    private List<LocalDate> availableDates;
    private int price;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static ReservationPostResponse from(ReservationPost post) {
        String child = post.getRegion().getName();
        String parent = post.getRegion().getParent() != null ? post.getRegion().getParent().getName() : null;
        String regionText = parent != null ? "(" + parent + ") " + child : child;

        // ✅ imageUrl 처리 (기본 이미지 fallback 포함)
        String imageUrl = post.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = switch (post.getType()) {
                case BOAT -> "/images/boat.png";
                case STAY -> "/images/stay.png";
                case ISLAND -> "/images/island.png";
                case FLOAT -> "/images/float.png";
                case ROCK -> "/images/rock.png";
                default -> "/images/default.jpg";
            };
        }

        return ReservationPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .region(regionText)
                .type(post.getType().name())
                .price(post.getPrice())
                .imageUrl(imageUrl)
                .createdAt(post.getCreatedAt())
                .availableDates(
                        post.getAvailableDates().stream()
                                .map(ReservationPostAvailableDate::getAvailableDate)
                                .toList()
                )
                .build();
    }
}

