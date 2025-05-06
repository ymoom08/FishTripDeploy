package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import com.fishtripplanner.dto.reservation.RegionDto;
import com.fishtripplanner.dto.reservation.ReservationCardDto;
import com.fishtripplanner.repository.RegionRepository;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationFilterController {

    private final RegionRepository regionRepository;
    private final ReservationPostService reservationPostService;

    /**
     * ✅ 지역 계층 구조 조회
     * - 부모-자식 관계를 갖는 지역 리스트 반환
     * - 지역 선택 모달에 사용
     */
    @GetMapping("/regions/hierarchy")
    public List<RegionDto> getRegionHierarchy() {
        return regionRepository.findAllWithChildrenOnly()
                .stream()
                .map(RegionDto::from)
                .toList();
    }

    /**
     * ✅ 등록된 어종 이름 목록 조회
     * - 어종 선택 모달에 사용
     */
    @GetMapping("/fish-types")
    public List<String> getFishTypes() {
        return reservationPostService.getFishTypeNames();
    }

    /**
     * ✅ 예약글 필터링 API
     * - type(필수) + regionId/date/fishType(선택)
     * - 필터 조합에 따라 ReservationPost 목록 반환
     */
    @GetMapping("/reservation")
    public List<ReservationCardDto> getFilteredCards(
            @RequestParam("type") String type, // 필수
            @RequestParam(value = "regionId", required = false) List<Long> regionIds,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "fishType", required = false) List<String> fishTypes,
            @RequestParam(value = "sort", defaultValue = "latest") String sortKey, // ✅ 정렬 파라미터
            Pageable pageable
    ) {
        // 🔹 문자열 → enum으로 변환
        ReservationType enumType = ReservationType.valueOf(type.toUpperCase());

        // 🔹 날짜 파싱
        LocalDate parsedDate = (dateStr != null && !dateStr.isBlank()) ? LocalDate.parse(dateStr) : null;

        // 🔹 빈 리스트는 null로 처리 (서비스에서 조건 분기 처리)
        List<Long> validRegionIds = (regionIds == null || regionIds.isEmpty()) ? null : regionIds;
        List<String> validFishTypes = (fishTypes == null || fishTypes.isEmpty()) ? null : fishTypes;



        // 🔹 서비스 호출
        Page<ReservationPost> page = reservationPostService.filterPosts(
                enumType, validRegionIds, parsedDate, validFishTypes, sortKey, pageable
        );

        // 🔹 DTO 변환 후 반환
        return page.stream()
                .map(ReservationCardDto::from)
                .toList();
    }

    // ✅ ReservationFilterController.java
    @GetMapping("/regions/names") // 혹은 "/regions/used"
    public List<String> getUsedRegionNames() {
        return reservationPostService.getUsedRegionNames();  // 서비스로 위임
    }
}
