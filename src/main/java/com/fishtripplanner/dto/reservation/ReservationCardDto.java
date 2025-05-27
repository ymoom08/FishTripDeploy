package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.entity.FishTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCardDto {

    private Long id;
    private String title;
    private String content;
    private String companyName;
    private String imageUrl;
    private String region;  // (부모) 자식 형식
    private List<String> fishTypes;

    public static ReservationCardDto from(ReservationPost post) {
        String regionText = null;

        if (post.getRegion() != null) {
            String child = post.getRegion().getName();
            String parent = post.getRegion().getParent() != null
                    ? post.getRegion().getParent().getName()
                    : null;
            regionText = parent != null ? "(" + parent + ") " + child : child;
        }

        // ✅ 이미지 경로 처리
        String imageUrl = post.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            switch (post.getType()) {
                case BOAT -> imageUrl = "/images/boat.jpg";
                case FLOAT -> imageUrl = "/images/float.png";
                case ISLAND -> imageUrl = "/images/island.jpg";
                case ROCK -> imageUrl = "/images/rock.jpg";
                case STAY -> imageUrl = "/images/stay.png";
                default -> imageUrl = "/images/default.jpg";
            }
        } else if (!imageUrl.startsWith("/uploads/") && !imageUrl.startsWith("/images/")) {
            // 예외 케이스 대비 (예: 파일명만 저장됐을 때)
            imageUrl = "/uploads/reservation_images/" + imageUrl;
        }

        return new ReservationCardDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCompanyName(),
                imageUrl,
                regionText,
                post.getFishTypes().stream()  // ✅ 수정된 부분
                        .map(FishTypeEntity::getName)
                        .collect(Collectors.toList())
        );
    }
}
