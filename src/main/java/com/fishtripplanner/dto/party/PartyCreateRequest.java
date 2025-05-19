package com.fishtripplanner.dto.party;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCreateRequest {
    private String title;
    private Long LeaderId;
    private String description;
    private String detail;
    private String region;
    private String departurePoint;
    private Double departureLat;
    private Double departureLng;
    private String destination;
    private Double destinationLat;
    private Double destinationLng;
    private LocalDateTime departureTime;
    private LocalDateTime deadline;
    private int maxParticipants;
    private int estimatedCost;
    private String memberDetail;
    private List<WaypointRequest> waypoints;
    private List<PartyMemberRequest> partyMembers;
    public List<PartyMemberRequest> getPartyMembers() {
        return partyMembers;
    }

}

