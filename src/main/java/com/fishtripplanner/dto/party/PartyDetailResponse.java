package com.fishtripplanner.dto.party;

import com.fishtripplanner.domain.party.Party;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PartyDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String region;

    private String departurePoint;
    private double departureLat;
    private double departureLng;

    private String destination;
    private double destinationLat;
    private double destinationLng;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private int estimatedCost;
    private int expectedDuration;
    private int maxParticipants;

    private List<String> stopovers;
    private List<Integer> stopoverStayTimes;

    private String leaderName;
    private List<String> participantNames;
    private List<String> approvedParticipantNames;

    public static PartyDetailResponse from(Party party) {
        return PartyDetailResponse.builder()
                .id(party.getId())
                .title(party.getTitle())
                .description(party.getDescription())
                .region(party.getRegion())
                .departurePoint(party.getDeparturePoint())
                .departureLat(party.getDepartureLat())
                .departureLng(party.getDepartureLng())
                .destination(party.getDestination())
                .destinationLat(party.getDestinationLat())
                .destinationLng(party.getDestinationLng())
                .departureTime(party.getDepartureTime())
                .arrivalTime(party.getArrivalTime())
                .estimatedCost(party.getEstimatedCost())
                .expectedDuration(party.getExpectedDuration())
                .maxParticipants(party.getMaxParticipants())
                .stopovers(party.getStopovers())
                .stopoverStayTimes(party.getStopoverStayTimes())
                .leaderName(party.getLeader().getUsername())
                .participantNames(party.getParticipants().stream().map(user -> user.getUsername()).toList())
                .approvedParticipantNames(party.getApprovedParticipants().stream().map(user -> user.getUsername()).toList())
                .build();
    }
}
