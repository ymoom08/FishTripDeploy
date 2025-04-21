package com.fishtripplanner.security;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = token.getPrincipal();

//        String kakaoId = user.getAttribute("id").toString();
        String nickname = ((Map<String, Object>) user.getAttribute("properties")).get("nickname").toString();
        String profileImage = ((Map<String, Object>) user.getAttribute("properties")).get("profile_image").toString();

        Optional<User> existing = userRepository.findByNickname(nickname);
        if (existing.isPresent()) {
            // 자동 로그인 처리
            redirectStrategy.sendRedirect(request, response, "/");
        } else {
            // 회원가입 폼으로 redirect, session에 정보 저장
            HttpSession session = request.getSession();
            session.setAttribute("oauth_nickname", nickname);
            session.setAttribute("oauth_profile_image", profileImage);


            redirectStrategy.sendRedirect(request, response, "/join/oauth");
        }
    }
}

