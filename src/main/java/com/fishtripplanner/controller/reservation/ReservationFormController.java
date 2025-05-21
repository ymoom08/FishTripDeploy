package com.fishtripplanner.controller.reservation;

import com.fishtripplanner.dto.ReservationPostRequest;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.ReservationPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationFormController {

    private final ReservationPostService reservationPostService;

    @PostMapping("/new")
    public String submitForm(@ModelAttribute ReservationPostRequest request,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        reservationPostService.createReservationPost(request, userDetails.getUser());
        return "redirect:/reservation/" + request.getType().toLowerCase();
    }
}
