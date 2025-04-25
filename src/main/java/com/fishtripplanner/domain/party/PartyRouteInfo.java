package com.fishtripplanner.domain.party;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyRouteInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "party_id")
    private Party party;

    private int totalDistance; // meters
    private int totalDuration; // seconds
    private int estimatedCost; // won

    @Column(columnDefinition = "TEXT")
    private String routeSummary;

    private LocalDateTime createdAt;
}
