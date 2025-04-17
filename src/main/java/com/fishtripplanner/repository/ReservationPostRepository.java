package com.fishtripplanner.repository;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.reservation.ReservationPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationPostRepository extends JpaRepository<ReservationPost, Long> {
    List<ReservationPost> findByOwnerId(Long ownerId);
}
