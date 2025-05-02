package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationPostRepository extends JpaRepository<ReservationPost, Long> {

    // ✅ 예약글 소유자 기준 조회 (내 예약글 조회용)
    List<ReservationPost> findByOwnerId(Long ownerId);

    // ✅ 타입 기준 전체 조회 (예: 선상낚시, 갯바위낚시 등) — 전체 조회 (List)
    List<ReservationPost> findByType(ReservationType type);

    // ✅ 타입 기준 전체 조회 — 페이지네이션 지원
    Page<ReservationPost> findByType(ReservationType type, Pageable pageable);

    // ✅ 타입 + 지역 ID 기준 조회
    @Query("""
        SELECT r
        FROM ReservationPost r
        WHERE r.type = :type
          AND r.region.id IN :regionIds
    """)
    Page<ReservationPost> findByTypeAndRegionIds(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            Pageable pageable
    );

    // ✅ 타입 + 어종 기준 조회 (지역, 날짜 제외)
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.fishTypeEntities f
        WHERE r.type = :type
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByFishTypes(
            @Param("type") ReservationType type,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 타입 + 날짜 + 어종 기준 조회 (지역 제외)
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        WHERE r.type = :type
          AND :date IN elements(r.availableDates)
          AND EXISTS (
            SELECT 1
            FROM r.fishTypeEntities f
            WHERE f.name IN :fishTypes
          )
    """)
    Page<ReservationPost> findByDateAndFishTypes(
            @Param("type") ReservationType type,
            @Param("date") LocalDate date,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 타입 + 지역 + 날짜 기준 예약글 조회
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND :date IN elements(r.availableDates)
    """)
    Page<ReservationPost> findByTypeAndRegionIdsAndDate(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    // ✅ 타입 + 지역 + 어종 기준 예약글 조회 (날짜 제외) ← 🔥 새로 추가된 부분
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.fishTypeEntities f
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByRegionIdsAndFishTypes(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 통합 필터 (type + regionIds + date + fishTypes 조합)
    // - 모든 필터는 NULL 가능
    // - availableDates 및 fishTypeEntities는 컬렉션이므로 JOIN으로 안전하게 처리
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        LEFT JOIN r.availableDates d
        LEFT JOIN r.fishTypeEntities f
        WHERE r.type = :type
          AND (:regionIds IS NULL OR r.region.id IN :regionIds)
          AND (:date IS NULL OR d = :date)
          AND (:fishTypes IS NULL OR f.name IN :fishTypes)
    """)
    Page<ReservationPost> findByFilters(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("date") LocalDate date,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 타입 + 날짜 기준 예약글 조회
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        WHERE r.type = :type
          AND :date IN elements(r.availableDates)
    """)
    Page<ReservationPost> findByTypeAndDate(
            @Param("type") ReservationType type,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    // ✅ 등록된 모든 어종 이름 반환 (모달 체크박스용)
    @Query("SELECT DISTINCT f.name FROM FishTypeEntity f")
    List<String> findAllFishTypeNames();

    // ✅ 등록된 예약글 기반 지역 이름 반환
    @Query("SELECT DISTINCT r.region.name FROM ReservationPost r")
    List<String> findAllRegionNames();
}
