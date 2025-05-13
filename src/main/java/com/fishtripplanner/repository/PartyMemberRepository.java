package com.fishtripplanner.repository;

import com.fishtripplanner.domain.party.PartyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {
}
