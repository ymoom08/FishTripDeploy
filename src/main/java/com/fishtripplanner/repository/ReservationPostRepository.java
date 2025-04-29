package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.domain.reservation.ReservationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationPostRepository extends JpaRepository<ReservationPost, Long> {

    // 🔥 예약글 소유자 기준 조회
    List<ReservationPost> findByOwnerId(Long ownerId);

    // 🔥 타입 기준 전체 조회 (List)
    List<ReservationPost> findByType(ReservationType type);

    // 🔥 타입 기준 전체 조회 (Pageable)
    Page<ReservationPost> findByType(ReservationType type, Pageable pageable);

    // 🔥 타입 + 지역 기준 조회 (Pageable)
    @Query("SELECT r FROM ReservationPost r WHERE r.type = :type AND r.region.id IN :regionIds")
    Page<ReservationPost> findByTypeAndRegionIds(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            Pageable pageable);

    // 🔥 전체 지역 목록 조회 (선택 사항)
    @Query("SELECT DISTINCT r.region FROM ReservationPost r")
    List<String> findAllRegions();

    // ✅ [추가] 타입 + 날짜 기준 조회 (Pageable)
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates ad
        WHERE r.type = :type
          AND ad = :date
        """)
    Page<ReservationPost> findByTypeAndDate(
            @Param("type") ReservationType type,
            @Param("date") LocalDate date,
            Pageable pageable);

    // ✅ [추가] 타입 + 지역 + 날짜 기준 조회 (Pageable)
    @Query("""
        SELECT DISTINCT r
        FROM ReservationPost r
        JOIN r.availableDates ad
        WHERE r.type = :type
          AND r.region.id IN :regionIds
          AND ad = :date
        """)
    Page<ReservationPost> findByTypeAndRegionIdsAndDate(
            @Param("type") ReservationType type,
            @Param("regionIds") List<Long> regionIds,
            @Param("date") LocalDate date,
            Pageable pageable);
}
