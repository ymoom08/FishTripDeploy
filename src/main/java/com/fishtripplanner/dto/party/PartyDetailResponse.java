package com.fishtripplanner.dto.party;

import com.fishtripplanner.domain.party.Party;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PartyDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String detail;
    private String region;

    private String departurePoint;
    private double departureLat;
    private double departureLng;

    private String destination;
    private double destinationLat;
    private double destinationLng;

    private LocalDateTime departureTime;
    private LocalDateTime deadline;
    private int estimatedCost;
    private int maxParticipants;

    private List<String> stopovers;
    private List<Integer> stopoverStayTimes;

    private String leaderName;
    private List<String> participantNames;
    private String memberDetail;

    public static PartyDetailResponse from(Party party) {
        return PartyDetailResponse.builder()
                .id(party.getId())
                .title(party.getTitle())
                .description(party.getDescription())
                .detail(party.getDetail())
                .region(party.getRegion())
                .departurePoint(party.getDeparturePoint())
                .departureLat(party.getDepartureLat())
                .departureLng(party.getDepartureLng())
                .destination(party.getDestination())
                .destinationLat(party.getDestinationLat())
                .destinationLng(party.getDestinationLng())
                .departureTime(party.getDepartureTime())
                .deadline(party.getDeadline())
                .estimatedCost(party.getEstimatedCost())
                .maxParticipants(party.getMaxParticipants())
                .stopovers(party.getWaypoints().stream().map(w -> w.getName()).collect(Collectors.toList()))
                .stopoverStayTimes(party.getWaypoints().stream().map(w -> w.getStayTime()).collect(Collectors.toList()))
                .leaderName(party.getLeader().getUsername())
                .participantNames(party.getPartyMembers().stream().map(u -> u.getUsername()).collect(Collectors.toList()))
                .memberDetail(party.getMemberDetail())
                .build();
    }
}
