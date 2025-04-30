package com.fishtripplanner.controller.Party;

import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.domain.party.Waypoint;
import com.fishtripplanner.domain.party.WaypointType;
import com.fishtripplanner.dto.party.PartyCreateRequest;
import com.fishtripplanner.repository.PartyRepository;
import com.fishtripplanner.repository.WaypointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyController {

    private final PartyRepository partyRepository;
    private final WaypointRepository waypointRepository;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("partyForm", new PartyCreateRequest());
        return "party/create";
    }

    @PostMapping("/save")
    @Transactional
    public String saveParty(@ModelAttribute PartyCreateRequest request) {
        Party party = Party.builder()
                .title(request.getTitle())
                .departurePoint(request.getDeparturePoint())
                .departureLat(request.getDepartureLat())
                .departureLng(request.getDepartureLng())
                .destination(request.getDestination())
                .destinationLat(request.getDestinationLat())
                .destinationLng(request.getDestinationLng())
                .build();

        partyRepository.save(party);

        if (request.getWaypoints() != null) {
            int idx = 1;
            for (var w : request.getWaypoints()) {
                Waypoint waypoint = Waypoint.builder()
                        .party(party)
                        .name(w.getName())
                        .lat(w.getLat())
                        .lng(w.getLng())
                        .type(WaypointType.WAYPOINT)
                        .orderIndex(idx++)
                        .stayTime(0)
                        .build();
                waypointRepository.save(waypoint);
            }
        }

        return "redirect:/"; // 저장 완료 후 메인으로 이동
    }
}

