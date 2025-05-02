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
     * - 주어진 필터 조건(type, regionIds, date, fishTypes)에 따라
     * - 가장 적절한 Repository 메서드를 선택하여 조회 수행
     */
    public Page<ReservationPost> filterPosts(
            ReservationType type,
            List<Long> regionIds,
            LocalDate date,
            List<String> fishTypes,
            Pageable pageable
    ) {
        boolean hasRegion = regionIds != null && !regionIds.isEmpty();
        boolean hasDate = date != null;
        boolean hasFish = fishTypes != null && !fishTypes.isEmpty();

        // ✅ 필터 조합 문자열 기반으로 switch-case 분기
        // - "true-true-true" 조합은 정확 일치(strict) 쿼리로 분기
        // - 나머지는 조건에 따라 각각 전용 메서드 호출
        return switch (String.format("%s-%s-%s", hasRegion, hasDate, hasFish)) {
            case "true-true-true"   -> reservationPostRepository.findByFiltersStrict(type, regionIds, date, fishTypes, pageable); // ✅ 정확 일치용
            case "true-true-false"  -> reservationPostRepository.findByTypeAndRegionIdsAndDate(type, regionIds, date, pageable);
            case "false-true-true"  -> reservationPostRepository.findByDateAndFishTypes(type, date, fishTypes, pageable);
            case "true-false-true"  -> reservationPostRepository.findByRegionIdsAndFishTypes(type, regionIds, fishTypes, pageable);
            case "false-false-true" -> reservationPostRepository.findByFishTypes(type, fishTypes, pageable);
            case "false-true-false" -> reservationPostRepository.findByTypeAndDate(type, date, pageable);
            case "true-false-false" -> reservationPostRepository.findByTypeAndRegionIds(type, regionIds, pageable);
            default                 -> reservationPostRepository.findByType(type, pageable); // 아무 조건도 없을 때 전체 조회
        };
    }

    /**
     * ✅ 등록된 어종 이름 리스트 반환 (정렬 포함)
     * - 어종 선택 모달 구성에 사용됨
     */
    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames()
                .stream()
                .sorted() // 가나다 순 정렬
                .toList();
    }

    /**
     * ✅ 등록된 지역 이름 리스트 반환
     * - 지역 필터 모달 구성에 사용됨
     */
    public List<String> getUsedRegionNames() {
        return reservationPostRepository.findAllRegionNames();
    }
}
