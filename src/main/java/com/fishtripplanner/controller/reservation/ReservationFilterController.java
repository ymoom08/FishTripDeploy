package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.RegionDto;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationFilterController {

    private final RegionRepository regionRepository;
    private final ReservationPostRepository reservationPostRepository;

    // ✅ 지역 계층 구조 반환
    @GetMapping("/regions/hierarchy")
    public List<RegionDto> getRegionHierarchy() {
        return regionRepository.findAllWithChildrenOnly()
                .stream()
                .map(RegionDto::from)
                .toList();
    }

    // ✅ 지역 + 날짜 필터 포함 예약 카드 조회 API
    @GetMapping("/reservation")
    public List<ReservationCardDto> getFilteredCards(
            @RequestParam(name = "type") String type,
            @RequestParam(name = "regionId", required = false) List<Long> regionIds,
            @RequestParam(name = "date", required = false) String date,
            Pageable pageable
    ) {
        ReservationType enumType;
        try {
            enumType = ReservationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 예약 타입입니다: " + type);
        }

        Page<ReservationPost> page;

        if (date != null && !date.isBlank()) {
            // 🔥 날짜 파라미터를 LocalDate로 변환
            LocalDate parsedDate;
            try {
                parsedDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("날짜 포맷이 올바르지 않습니다. (yyyy-MM-dd 형식이어야 합니다)");
            }

            // 🔥 날짜 + 지역 + 타입 필터
            if (regionIds != null && !regionIds.isEmpty()) {
                page = reservationPostRepository.findByTypeAndRegionIdsAndDate(enumType, regionIds, parsedDate, pageable);
            } else {
                page = reservationPostRepository.findByTypeAndDate(enumType, parsedDate, pageable);
            }

        } else {
            // 🔥 날짜 없이 기존 필터
            if (regionIds != null && !regionIds.isEmpty()) {
                page = reservationPostRepository.findByTypeAndRegionIds(enumType, regionIds, pageable);
            } else {
                page = reservationPostRepository.findByType(enumType, pageable);
            }
        }

        return page.stream()
                .map(ReservationCardDto::from)
                .toList();
    }
}
