package com.fishtripplanner.domain.party;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Waypoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double lat;
    private double lng;
    private int stayTime;
    private int orderIndex;

    @Enumerated(EnumType.STRING)
    private WaypointType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;
}