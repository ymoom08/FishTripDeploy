package com.fishtripplanner.dto.party;

import com.fishtripplanner.domain.party.Party;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PartyResponse {
    private Long id;
    private String title;
    private String description;
    private String region;
    private String departurePoint;
    private Double departureLat;
    private Double departureLng;
    private String destination;
    private LocalDateTime departureTime;
    private int maxParticipants;
    private int estimatedCost;
    private List<String> stopovers;
    private List<Integer> stopoverStayTimes;

    public static PartyResponse from(Party party) {
        return PartyResponse.builder()
                .id(party.getId())
                .title(party.getTitle())
                .description(party.getDescription())
                .region(party.getRegion())
                .departurePoint(party.getDeparturePoint())
                .departureLat(party.getDepartureLat())
                .departureLng(party.getDepartureLng())
                .destination(party.getDestination())
                .departureTime(party.getDepartureTime())
                .maxParticipants(party.getMaxParticipants())
                .estimatedCost(party.getEstimatedCost())
                .stopovers(party.getStopovers())
                .stopoverStayTimes(party.getStopoverStayTimes())
                .build();
    }
}
