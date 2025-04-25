package com.fishtripplanner.domain.party;

import com.fishtripplanner.domain.reservation.ReservationPost;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @ManyToOne
    @JoinColumn(name = "reservation_post_id")
    private ReservationPost reservationPost;

    private LocalDate reservationDate;
    private int participantCount;
    private String memo;
}
