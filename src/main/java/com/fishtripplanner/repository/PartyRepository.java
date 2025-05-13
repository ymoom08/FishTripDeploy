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

    // 특정 파티에 특정 유저가 참가한 적이 있는지 확인 (중복 참가 방지)
    Optional<Party> findByIdAndPartyMembers_User_Id(Long partyId, Long userId);

    // 전체 파티를 생성일자 기준으로 내림차순 정렬
    List<Party> findAllByOrderByCreatedAtDesc();
}
