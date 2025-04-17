package com.fishtripplanner.api.party;

import com.fishtripplanner.dto.party.*;
import com.fishtripplanner.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @PostMapping("/create")
    public ResponseEntity<PartyResponse> createParty(@RequestBody PartyCreateRequest request) {
        return ResponseEntity.ok(partyService.createParty(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartyResponse> getPartyById(@PathVariable Long id) {
        return ResponseEntity.ok(partyService.getPartyById(id));
    }

    @GetMapping("/list")
    public ResponseEntity<List<PartyResponse>> getParties(
            @RequestParam(required = false) String region,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        return ResponseEntity.ok(partyService.getPartyList(region, sortBy, order));
    }

    @PostMapping("/join")
    public ResponseEntity<String> joinParty(@RequestBody PartyJoinRequest request) {
        partyService.requestJoin(request.getPartyId(), request.getUserId());
        return ResponseEntity.ok("참가 요청 완료");
    }

    // ✅ 참가 승인
    @PostMapping("/approve")
    public ResponseEntity<String> approveParticipant(@RequestParam Long partyId, @RequestParam Long userId) {
        partyService.approveParticipant(partyId, userId);
        return ResponseEntity.ok("참가 승인 완료");
    }

    // ✅ 참가 거절
    @PostMapping("/reject")
    public ResponseEntity<String> rejectParticipant(@RequestParam Long partyId, @RequestParam Long userId) {
        partyService.rejectParticipant(partyId, userId);
        return ResponseEntity.ok("참가 거절 완료");
    }
    // ✅ 승인된 참가자 목록 조회
    @GetMapping("/participants")
    public ResponseEntity<List<String>> getApprovedParticipants(@RequestParam Long partyId) {
        return ResponseEntity.ok(partyService.getApprovedUsernames(partyId));
    }
    // ✅ 파티방 게시글 등록
    @PostMapping("/board")
    public ResponseEntity<String> writeBoard(@RequestBody PartyBoardRequest request) {
        partyService.writeBoard(request);
        return ResponseEntity.ok("게시글 등록 완료");
    }

    // ✅ 파티방 게시글 조회
    @GetMapping("/board")
    public ResponseEntity<List<String>> getBoard(@RequestParam Long partyId) {
        return ResponseEntity.ok(partyService.getBoardMessages(partyId));
    }
    // ✅ 채팅 로그 저장
    @PostMapping("/chat")
    public ResponseEntity<String> saveChat(@RequestBody PartyChatRequest request) {
        partyService.saveChatMessage(request);
        return ResponseEntity.ok("채팅 메시지 저장 완료");
    }

    @GetMapping("/chat")
    public ResponseEntity<List<String>> getChat(@RequestParam Long partyId) {
        return ResponseEntity.ok(partyService.getChatMessages(partyId));
    }
    // ✅ 강퇴
    @PostMapping("/kick")
    public ResponseEntity<String> kickUser(@RequestParam Long partyId, @RequestParam Long targetUserId, @RequestParam Long leaderId) {
        partyService.kickUser(partyId, targetUserId, leaderId);
        return ResponseEntity.ok("강퇴 완료");
    }
    // ✅ 모집 조기 마감
    @PostMapping("/close")
    public ResponseEntity<String> closeParty(@RequestParam Long partyId, @RequestParam Long leaderId) {
        partyService.closeParty(partyId, leaderId);
        return ResponseEntity.ok("모집이 마감되었습니다.");
    }

}

