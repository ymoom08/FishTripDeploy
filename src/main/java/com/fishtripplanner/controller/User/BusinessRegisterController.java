package com.fishtripplanner.controller.User;
import com.fishtripplanner.domain.BusinessInfo;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.UserRole;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class BusinessRegisterController {

    private final UserRepository userRepository;



    @PostMapping("/register/business")
    public String registerBusinessUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String name,// name은 BusinessInfo로 넘길 예정
            @RequestParam String nickname,
            @RequestParam String company,
            @RequestParam String address,
            @RequestParam String address2,
            @RequestParam String service,
            @RequestParam Number phonenumber
    ) {
        // 가입일시
        LocalDateTime now = LocalDateTime.now();

        // 1. User 생성
        User user = User.builder()
                .username(username)
                .password(password) // 비밀번호 암호화
                .email(email)
                .nickname(nickname)
                .name(name)
                .phonenumber(phonenumber.toString())
                .address(address + " " + address2)
                .role(UserRole.OWNER) // 예: BUSINESS 또는 OWNER 같은 enum 값
                .createdAt(now)
                .build();

        // 2. BusinessInfo 생성 및 연결
        BusinessInfo businessInfo = new BusinessInfo();
        businessInfo.setUser(user);
        businessInfo.setCompanyName(company);
        businessInfo.setAddress(address + " " + address2);
        businessInfo.setServiceTypes(Set.of(service)); // 다중 선택이면 Set.of(...)

        user.setBusinessInfo(businessInfo); // 양방향 연결

        // 3. 저장
        userRepository.save(user); // Cascade.ALL 덕분에 BusinessInfo도 함께 저장됨

        return "redirect:/login";
    }
}



