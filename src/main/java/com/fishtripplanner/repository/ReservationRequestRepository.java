package com.fishtripplanner.repository;

import com.fishtripplanner.domain.reservation.ReservationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRequestRepository extends JpaRepository<ReservationRequest, Long> {
    List<ReservationRequest> findByReservationPostId(Long postId);
    List<ReservationRequest> findByUserId(Long userId);
}