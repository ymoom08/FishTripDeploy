package com.fishtripplanner.dto.party;

import lombok.Getter;

@Getter
public class PartyBoardRequest {
    private Long partyId;
    private Long userId;
    private String message;
}
