package com.fishtripplanner.controller.join;

import com.fishtripplanner.domain.BusinessInfo;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.UserRole;
import com.fishtripplanner.dto.user.JoinRequest;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class JoinController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/join")
    public String joinChoice() {
        return "joinChoice";
    }

    @GetMapping("/join/normal")
    public String generalJoinForm() {
        return "joinNormal";
    }

    @PostMapping("/join/normal")
    public String registerNormal(@ModelAttribute JoinRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .address(request.getAddress())
                .name(request.getName())
                .nickname(request.getNickname())
                .phonenumber(request.getPhonenumber())
                .gender(request.getGender())
                .age(request.getAge())
                .birthyear(request.getBirthyear())
                .birthday(request.getBirthday())
                .role(UserRole.NORMAL)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/join/business")
    public String businessJoinForm() {
        return "joinBusiness";
    }

    @PostMapping("/join/business")
    public String registerBusiness(@ModelAttribute JoinRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .address(request.getAddress())
                .name(request.getName())
                .nickname(request.getNickname())
                .phonenumber(request.getPhonenumber())
                .gender(request.getGender())
                .age(request.getAge())
                .birthyear(request.getBirthyear())
                .birthday(request.getBirthday())
                .role(UserRole.OWNER)
                .createdAt(LocalDateTime.now())
                .build();

        BusinessInfo businessInfo = BusinessInfo.builder()
                .companyName(request.getCompanyName())
                .businessNumber(request.getBusinessNumber())
                .serviceTypes(Collections.singleton(request.getServiceType()))
                .user(user)
                .build();

        user.setBusinessInfo(businessInfo);
        userRepository.save(user);

        return "redirect:/login";
    }

    @GetMapping("/join/oauth")
    public String registerWithOAuth(HttpSession session, Model model) {
        model.addAttribute("nickname", session.getAttribute("oauth_nickname"));
        model.addAttribute("profileImage", session.getAttribute("oauth_profile_image"));
        model.addAttribute("email", session.getAttribute("oauth_email"));
        model.addAttribute("gender", session.getAttribute("oauth_gender"));
        model.addAttribute("age", session.getAttribute("oauth_age"));
        model.addAttribute("birthday", session.getAttribute("oauth_birthday"));
        model.addAttribute("birthyear", session.getAttribute("oauth_birthyear"));
        model.addAttribute("mobile", session.getAttribute("oauth_mobile"));
        model.addAttribute("name", session.getAttribute("oauth_name"));
        return "join/oauthJoinForm";
    }

    @PostMapping("/join/oauth")
    public String registerOAuthUser(@ModelAttribute JoinRequest request, HttpSession session) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email((String) session.getAttribute("oauth_email"))
                .address(request.getAddress())
                .name((String) session.getAttribute("oauth_name"))
                .nickname((String) session.getAttribute("oauth_nickname"))
                .phonenumber((String) session.getAttribute("oauth_mobile"))
                .gender((String) session.getAttribute("oauth_gender"))
                .age((String) session.getAttribute("oauth_age"))
                .birthyear((String) session.getAttribute("oauth_birthyear"))
                .birthday((String) session.getAttribute("oauth_birthday"))
                .role(UserRole.NORMAL)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        session.invalidate();
        return "redirect:/login";
    }
}
