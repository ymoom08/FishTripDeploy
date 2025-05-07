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
     * - 필터 조건(type, regionIds, date, fishTypes)에 따라 쿼리 분기
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
        // ✅ 정렬 기준 처리
        Sort sort = switch (sortKey) {
            case "priceAsc"  -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            case "latest"    -> Sort.by("createdAt").descending();
            default          -> Sort.by("createdAt").descending();
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        boolean hasRegion = regionIds != null && !regionIds.isEmpty();
        boolean hasDate = date != null;
        boolean hasFish = fishTypes != null && !fishTypes.isEmpty();

        return switch (String.format("%s-%s-%s", hasRegion, hasDate, hasFish)) {
            case "true-true-true"   -> reservationPostRepository.findByFiltersStrict(
                    type, regionIds, date, fishTypes, sortedPageable);
            case "true-true-false"  -> reservationPostRepository.findByTypeAndRegionIdsAndDate(
                    type, regionIds, date, sortedPageable);
            case "false-true-true"  -> reservationPostRepository.findByDateAndFishTypes(
                    type, date, fishTypes, sortedPageable);
            case "true-false-true"  -> reservationPostRepository.findByRegionIdsAndFishTypes(
                    type, regionIds, fishTypes, sortedPageable);
            case "false-false-true" -> reservationPostRepository.findByFishTypes(
                    type, fishTypes, sortedPageable);
            case "false-true-false" -> reservationPostRepository.findByTypeAndDate(
                    type, date, sortedPageable);
            case "true-false-false" -> reservationPostRepository.findByTypeAndRegionIds(
                    type, regionIds, sortedPageable);
            default                 -> reservationPostRepository.findByFilters(
                    type, regionIds, date, fishTypes, keyword, sortedPageable);
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
