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
@Builder
public class BusinessInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String companyName;
    private String businessNumber;
    private String address;

}

