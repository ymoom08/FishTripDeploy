package com.fishtripplanner.domain.party;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    private String title;
    private String description;
    private String detail; // 상세 설명 추가
    private String region;
    private String departurePoint;
    private double departureLat;
    private double departureLng;
    private String destination;
    private double destinationLat;
    private double destinationLng;
    private LocalDateTime departureTime;
    private LocalDateTime deadline; // 마감일시 추가
    private int maxParticipants;
    private int estimatedCost;
    private String memberDetail; // 모집 대상 설명 추가
    private boolean closed = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Waypoint> waypoints = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "reservation_post_id")
    private ReservationPost reservationPost;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMember> partyMembers = new ArrayList<>();

    public void addPartyMember(PartyMember partyMember) {
        this.partyMembers.add(partyMember);
        partyMember.setParty(this);
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
        if (waypoints != null) {
            for (Waypoint waypoint : waypoints) {
                waypoint.setParty(this);
            }
        }
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
