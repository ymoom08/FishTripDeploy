package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationPostRepository extends JpaRepository<ReservationPost, Long> {

    // ✅ 예약글 소유자 기준 조회 (내 예약글 조회용)
    List<ReservationPost> findByOwnerId(Long ownerId);

    // ✅ 타입 기준 전체 조회 (예: 선상낚시, 갯바위낚시 등)
    List<ReservationPost> findByType(ReservationType type);

    // ✅ 타입 기준 페이지 조회
    Page<ReservationPost> findByType(ReservationType type, Pageable pageable);

    // ✅ 타입 + 지역
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

    // ✅ 타입 + 어종
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByFishTypes(
            @Param("type") ReservationType type,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 타입 + 날짜 + 어종
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND d.availableDate = :date
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByDateAndFishTypes(
            @Param("type") ReservationType type,
            @Param("date") LocalDate date,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 타입 + 지역 + 날짜
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND d.availableDate = :date
    """)
    Page<ReservationPost> findByTypeAndRegionIdsAndDate(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    // ✅ 타입 + 지역 + 어종
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.fishTypes f
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

    // ✅ 타입 + 지역 + 날짜 + 어종 (정확 일치)
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        JOIN r.fishTypes f
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND d.availableDate = :date
          AND f.name IN :fishTypes
    """)
    Page<ReservationPost> findByFiltersStrict(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("date") LocalDate date,
            @Param("fishTypes") List<String> fishTypes,
            Pageable pageable
    );

    // ✅ 타입 + 날짜
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates d
        WHERE r.type = :type
          AND d.availableDate = :date
    """)
    Page<ReservationPost> findByTypeAndDate(
            @Param("type") ReservationType type,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    // ✅ 통합 필터 (느슨한 조건)
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        LEFT JOIN r.availableDates d
        LEFT JOIN r.fishTypes f
        WHERE r.type = :type
          AND (:regionIds IS NULL OR r.region.id IN :regionIds)
          AND (:date IS NULL OR d.availableDate = :date)
          AND (:fishTypes IS NULL OR f.name IN :fishTypes)
          AND (
              :keyword IS NULL
               OR LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR EXISTS (
                   SELECT 1
                   FROM r.fishTypes f2
                   WHERE LOWER(f2.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               )
               OR LOWER(r.region.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<ReservationPost> findByFilters(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("date") LocalDate date,
            @Param("fishTypes") List<String> fishTypes,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ✅ 어종 이름 목록 조회
    @Query("SELECT DISTINCT f.name FROM FishTypeEntity f")
    List<String> findAllFishTypeNames();

    // ✅ 지역 이름 목록 조회
    @Query("SELECT DISTINCT r.region.name FROM ReservationPost r")
    List<String> findAllRegionNames();

    // ✅ 상세 조회 시 연관 엔티티 포함
    @Query("""
        SELECT r FROM ReservationPost r
        LEFT JOIN FETCH r.fishTypes
        LEFT JOIN FETCH r.availableDates
        WHERE r.id = :id
    """)
    Optional<ReservationPost> findByIdWithFishTypesAndDate(@Param("id") Long id);
}
