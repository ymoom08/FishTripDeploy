package com.fishtripplanner.dto.party;

import lombok.Getter;

@Getter
public class PartyChatRequest {
    private Long partyId;
    private Long userId;
    private String message;
}