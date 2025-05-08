package com.fishtripplanner.domain.reservation;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.entity.FishTypeEntity;
import com.fishtripplanner.entity.RegionEntity;
import jakarta.persistence.*;
import lombok.*;

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

    private int price;

    private String imageUrl;

    private LocalDateTime createdAt;

    private String companyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @ManyToMany
    @JoinTable(
            name = "reservationpost_fishtype",
            joinColumns = @JoinColumn(name = "reservationpost_id"),
            inverseJoinColumns = @JoinColumn(name = "fish_type_id")
    )
    private List<FishTypeEntity> fishTypes;


    @OneToMany(mappedBy = "reservationPost")
    private List<ReservationPostAvailableDate> availableDates;




}



