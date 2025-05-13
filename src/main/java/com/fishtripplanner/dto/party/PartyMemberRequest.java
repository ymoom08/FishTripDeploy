package com.fishtripplanner.dto.party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyMemberRequest {
    private String username;     // 사용자의 식별자
    private String status;       // 참가 상태 (예: 승인됨, 대기 중 등)
    private LocalDateTime joinedAt;
}
