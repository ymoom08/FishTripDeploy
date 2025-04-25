package com.fishtripplanner.dto.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
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

        return new ReservationCardDto(
                post.getTitle(),
                post.getContent(),
                post.getCompanyName(),
                post.getImageUrl(),
                regionText,
                post.getFishTypeEntities().stream()
                        .map(FishTypeEntity::getName)
                        .collect(Collectors.toList())
        );
    }
}
