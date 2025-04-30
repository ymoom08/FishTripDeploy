// ✅ ReservationPostService.java (서비스 계층 - 예약글 필터 비즈니스 로직 담당)
package com.fishtripplanner.service;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationPostService {

    private final ReservationPostRepository reservationPostRepository;

    /**
     * ✅ 예약글 필터링 비즈니스 로직
     * 주어진 필터 조건(type, regionIds, date, fishTypes)에 따라
     * 가장 적절한 Repository 메서드를 선택하여 조회 수행.
     */
    public Page<ReservationPost> filterPosts(
            ReservationType type,             // 예약 타입 (boat, rock 등)
            List<Long> regionIds,             // 지역 ID 리스트
            LocalDate date,                   // 예약 가능한 날짜
            List<String> fishTypes,           // 어종 이름 리스트
            Pageable pageable                 // 페이지네이션 객체
    ) {
        boolean hasRegion = regionIds != null && !regionIds.isEmpty();
        boolean hasDate = date != null;
        boolean hasFish = fishTypes != null && !fishTypes.isEmpty();

        // ✅ [1] 모든 필터 조건이 있는 경우
        if (hasRegion && hasDate && hasFish) {
            return reservationPostRepository.findByFilters(type, regionIds, date, fishTypes, pageable);

            // ✅ [2] 지역 + 날짜만 있는 경우
        } else if (hasRegion && hasDate) {
            return reservationPostRepository.findByTypeAndRegionIdsAndDate(type, regionIds, date, pageable);

            // ✅ [3] 날짜 + 어종만 있는 경우 (지역은 제외)
        } else if (!hasRegion && hasDate && hasFish) {
            return reservationPostRepository.findByDateAndFishTypes(type, date, fishTypes, pageable);

            // ✅ [4] 날짜만 있는 경우
        } else if (hasDate) {
            return reservationPostRepository.findByTypeAndDate(type, date, pageable);

            // ✅ [5] 지역만 있는 경우
        } else if (hasRegion) {
            return reservationPostRepository.findByTypeAndRegionIds(type, regionIds, pageable);

            // ✅ [6] 아무 조건도 없을 경우 (기본 조회)
        } else {
            return reservationPostRepository.findByType(type, pageable);
        }
    }

    /**
     * ✅ 등록된 어종 이름 리스트 반환 (정렬 포함)
     * 어종 모달 및 선택 필터 구성에 사용됨.
     */
    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames()
                .stream()
                .sorted() // 가나다 순 정렬
                .toList();
    }
}
