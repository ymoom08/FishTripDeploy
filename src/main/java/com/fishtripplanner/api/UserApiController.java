
package com.fishtripplanner.api;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.dto.user.JoinRequest;
import com.fishtripplanner.dto.user.LoginRequest;
import com.fishtripplanner.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {

    private final UserRepository userRepository;

    //여기부터는 04월 17일부터 작업한 거예요!
    @ResponseBody // ✅ 이거 꼭 붙이기!
    @GetMapping("/check-id")
    public Map<String, Boolean> checkId(@RequestParam("username") String username) {
        boolean exists = userRepository.existsByUsername(username);
        return Map.of("exists", exists);
    }
}
