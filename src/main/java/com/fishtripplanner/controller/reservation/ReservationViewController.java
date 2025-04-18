package com.fishtripplanner.controller.reservation;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/reservation")
public class ReservationViewController {

    @GetMapping("")
    public String reservationPage() {
        return "reservation";
    }

    @GetMapping({"/boat", "/stay", "/float", "/island", "/rock"})
    public String reservationDetailPage(@RequestParam(name = "page", defaultValue = "0") int page,
                                        Model model,
                                        HttpServletRequest request) {

        String path = request.getRequestURI(); // e.g., /reservation/boat
        String type = path.substring(path.lastIndexOf("/") + 1); // e.g., "boat"

        List<String> allAds = Arrays.asList("ad1", "ad2", "ad3", "ad4", "ad5");
        int pageSize = 4;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allAds.size());
        List<String> pagedAds = allAds.subList(start, end);
        int totalPages = (int)Math.ceil((double)allAds.size() / pageSize);

        model.addAttribute("ads", pagedAds);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "reservation_detail/reservation_" + type;
    }
}
