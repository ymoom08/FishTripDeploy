package com.fishtripplanner.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String companyName;
    private String address;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> serviceTypes; // 선상, 갯바위, 섬 여객선, 선외기 대여, 민박집, 캠핑장!
}


