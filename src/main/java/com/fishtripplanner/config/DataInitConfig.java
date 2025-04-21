package com.fishtripplanner.config;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.UserRole;
import com.fishtripplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initTestUser() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("1234"))
                        .email("admin@example.com")
                        .role(UserRole.ADMIN)
                        .address("서울")
                        .name("관리자")
                        .nickname("Master")
                        .phonenumber("01099999999")
                        .build();
                userRepository.save(admin);
                System.out.println("✅ 테스트 관리자 계정 추가됨! (admin / 1234)");
            }
        };
    }
}
