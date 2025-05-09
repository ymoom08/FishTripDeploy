package com.fishtripplanner.service;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationPostService {

    private final ReservationPostRepository reservationPostRepository;

    /**
     * ✅ 예약글 필터링 비즈니스 로직
     * - 정렬 키(sortKey)에 따라 정렬 기준 동적 생성
     * - 필터 조건(type, regionIds, date, fishTypes, keyword)에 따라 쿼리 분기
     */
    public Page<ReservationPost> filterPosts(
            ReservationType type,
            List<Long> regionIds,
            LocalDate date,
            List<String> fishTypes,
            String keyword,
            String sortKey,
            Pageable pageable
    ) {
        // ✅ null-safe 처리
        List<Long> safeRegionIds = (regionIds == null || regionIds.isEmpty()) ? null : regionIds;
        List<String> safeFishTypes = (fishTypes == null || fishTypes.isEmpty()) ? null : fishTypes;
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword;

        // ✅ 정렬 기준 처리
        Sort sort = switch (sortKey) {
            case "priceAsc"  -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "latest"    -> Sort.by("createdAt").descending();
            default          -> Sort.by("createdAt").descending();
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        boolean hasRegion = safeRegionIds != null;
        boolean hasDate = date != null;
        boolean hasFish = safeFishTypes != null;

        String conditionKey = String.format("%s-%s-%s", hasRegion, hasDate, hasFish);

        // ✅ 조건에 따라 분기
        return switch (conditionKey) {
            case "true-true-true"   -> reservationPostRepository.findByFiltersStrict(
                    type, safeRegionIds, date, safeFishTypes, sortedPageable);
            case "true-true-false"  -> reservationPostRepository.findByTypeAndRegionIdsAndDate(
                    type, safeRegionIds, date, sortedPageable);
            case "false-true-true"  -> reservationPostRepository.findByDateAndFishTypes(
                    type, date, safeFishTypes, sortedPageable);
            case "true-false-true"  -> reservationPostRepository.findByRegionIdsAndFishTypes(
                    type, safeRegionIds, safeFishTypes, sortedPageable);
            case "false-false-true" -> reservationPostRepository.findByFishTypes(
                    type, safeFishTypes, sortedPageable);
            case "false-true-false" -> reservationPostRepository.findByTypeAndDate(
                    type, date, sortedPageable);
            case "true-false-false" -> reservationPostRepository.findByTypeAndRegionIds(
                    type, safeRegionIds, sortedPageable);
            default -> {
                // 🔥 모든 필터가 없는 경우에는 전체 조회 (이걸 안 하면 오류 남!)
                if (safeRegionIds == null && date == null && safeFishTypes == null && safeKeyword == null) {
                    yield reservationPostRepository.findByType(type, sortedPageable);
                }
                yield reservationPostRepository.findByFilters(
                        type, safeRegionIds, date, safeFishTypes, safeKeyword, sortedPageable);
            }
        };
    }

    public List<String> getFishTypeNames() {
        return reservationPostRepository.findAllFishTypeNames()
                .stream()
                .sorted()
                .toList();
    }

    public List<String> getUsedRegionNames() {
        return reservationPostRepository.findAllRegionNames();
    }
}

