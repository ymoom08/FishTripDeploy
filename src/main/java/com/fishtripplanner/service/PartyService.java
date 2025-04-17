package com.fishtripplanner.service;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.party.Party;
import com.fishtripplanner.domain.party.PartyStatus;
import com.fishtripplanner.domain.reservation.ReservationPost;
import com.fishtripplanner.dto.party.*;
import com.fishtripplanner.repository.PartyRepository;
import com.fishtripplanner.repository.UserRepository;
import com.fishtripplanner.repository.ReservationPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartyService {

    private final PartyRepository partyRepository;
    private final UserRepository userRepository;
    private final ReservationPostRepository reservationPostRepository;

    public PartyResponse createParty(PartyCreateRequest request) {
        User leader = userRepository.findById(request.getLeaderId()).orElseThrow();

        // 예약글 조회 및 연결
        ReservationPost reservationPost = reservationPostRepository.findById(request.getReservationPostId())
                .orElseThrow(() -> new RuntimeException("예약글을 찾을 수 없습니다."));

        // 파티 객체 생성 및 예약글 참조
        Party party = Party.builder()
                .leader(leader)
                .title(request.getTitle())
                .description(request.getDescription())
                .region(request.getRegion())
                .departurePoint(request.getDeparturePoint())
                .departureLat(request.getDepartureLat())
                .departureLng(request.getDepartureLng())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .maxParticipants(request.getMaxParticipants())
                .estimatedCost(request.getEstimatedCost())
                .stopovers(request.getStopovers())
                .stopoverStayTimes(request.getStopoverStayTimes())
                .createdAt(LocalDateTime.now())
                .board(new ArrayList<>())
                .chat(new ArrayList<>())
                .reservationPost(reservationPost) // 예약글 참조
                .build();

        return PartyResponse.from(partyRepository.save(party));
    }

    public PartyResponse getPartyById(Long id) {
        Party party = partyRepository.findById(id).orElseThrow();
        return PartyResponse.from(party);
    }

    public List<PartyResponse> getPartyList(String region, String sortBy, String order) {
        List<Party> list = (region == null || region.isBlank())
                ? partyRepository.findAll()
                : partyRepository.findByRegion(region);

        Comparator<Party> comparator = switch (sortBy) {
            case "estimatedCost" -> Comparator.comparingInt(Party::getEstimatedCost);
            case "departureTime" -> Comparator.comparing(Party::getDepartureTime);
            case "arrivalTime" -> Comparator.comparing(Party::getArrivalTime);
            default -> Comparator.comparing(Party::getCreatedAt);
        };

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return list.stream()
                .sorted(comparator)
                .map(PartyResponse::from)
                .collect(Collectors.toList());
    }

    public void requestJoin(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        party.getParticipants().add(user);
        partyRepository.save(party);
    }

    public void approveParticipant(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        party.getApprovedParticipants().add(user);
        partyRepository.save(party);
    }

    public void rejectParticipant(Long partyId, Long userId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        party.getParticipants().remove(user);
        partyRepository.save(party);
    }

    public List<String> getApprovedUsernames(Long partyId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        return party.getApprovedParticipants().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    public void writeBoard(PartyBoardRequest request) {
        Party party = partyRepository.findById(request.getPartyId()).orElseThrow();
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        party.getBoard().add(user.getUsername() + ": " + request.getMessage());
        partyRepository.save(party);
    }

    public List<String> getBoardMessages(Long partyId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        return party.getBoard();
    }

    public void saveChatMessage(PartyChatRequest request) {
        Party party = partyRepository.findById(request.getPartyId()).orElseThrow();
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        party.getChat().add(user.getUsername() + ": " + request.getMessage());
        partyRepository.save(party);
    }

    public List<String> getChatMessages(Long partyId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        return party.getChat();
    }

    public void kickUser(Long partyId, Long targetUserId, Long leaderId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        if (!party.getLeader().getId().equals(leaderId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        User target = userRepository.findById(targetUserId).orElseThrow();
        party.getApprovedParticipants().remove(target);
        party.getParticipants().remove(target);
        partyRepository.save(party);
    }

    public void closeParty(Long partyId, Long leaderId) {
        Party party = partyRepository.findById(partyId).orElseThrow();
        if (!party.getLeader().getId().equals(leaderId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        party.setStatus(PartyStatus.CLOSED);
        partyRepository.save(party);
    }

    public PartyDetailResponse getPartyDetail(Long id) {
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("파티를 찾을 수 없습니다."));
        return PartyDetailResponse.from(party);
    }
}

