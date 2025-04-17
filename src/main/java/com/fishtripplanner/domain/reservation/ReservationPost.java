package com.fishtripplanner.domain.reservation;

import com.fishtripplanner.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    private ReservationType type; // 선상, 갯바위, 섬, 좌대, 숙박 등

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String region;

    @ElementCollection
    private List<LocalDate> availableDates;

    private int price;

    private String imageUrl;

    private LocalDateTime createdAt;
}


