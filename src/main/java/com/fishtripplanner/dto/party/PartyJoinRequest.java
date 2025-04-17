package com.fishtripplanner.dto.party;

import lombok.Getter;

@Getter
public class PartyJoinRequest {
    private Long userId;
    private Long partyId;
}

