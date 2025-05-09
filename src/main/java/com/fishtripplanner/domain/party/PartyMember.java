package com.fishtripplanner.domain.party;

import com.fishtripplanner.domain.User;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username")
    private User user;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    private LocalDateTime joinedAt;

    public String getUsername() {
        return user.getUsername();
    }
}
