package com.fishtripplanner.api.user;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.UserRole;
import com.fishtripplanner.dto.user.JoinRequest;
import com.fishtripplanner.dto.user.LoginRequest;
import com.fishtripplanner.dto.user.UserResponse;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(JoinRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // 실제로는 비밀번호 해싱 필요
                .email(request.getEmail())
                .address(request.getAddress())
                .role(UserRole.NORMAL)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public boolean login(LoginRequest request) {
        return userRepository.findByUsernameAndPassword(
                request.getUsername(), request.getPassword()).isPresent();
    }
}
