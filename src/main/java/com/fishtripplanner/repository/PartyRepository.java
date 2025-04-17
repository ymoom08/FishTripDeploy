package com.fishtripplanner.repository;

import com.fishtripplanner.domain.party.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {

    // 지역으로 검색 (필터링용)
    List<Party> findByRegion(String region);

    // 파티장으로 검색 (자신이 만든 파티 조회용)
    List<Party> findByLeaderId(Long leaderId);

    // 모집 마감 여부로 검색 (예: 마이페이지에서 마감된 파티만 보기)
    List<Party> findByClosed(boolean closed);

    // 특정 파티와 유저가 매칭되는지 확인 (중복 참가 방지 등)
    Optional<Party> findByIdAndParticipants_Id(Long partyId, Long userId);

    // 참가 승인된 사용자 목록 포함 여부 (중복 승인 방지 등)
    Optional<Party> findByIdAndApprovedParticipants_Id(Long partyId, Long userId);

    // 정렬을 위한 전체 파티 조회 (region 없이)
    List<Party> findAllByOrderByCreatedAtDesc();
}
