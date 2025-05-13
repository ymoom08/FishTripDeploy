package com.fishtripplanner.controller;

import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final PostRepository postRepository;

    @GetMapping("/")
    public String rootRedirectToMain(Model model) {
        List<Post> posts = postRepository.findTop10ByOrderByCreatedAtDesc();
        List<Post> popularPosts = postRepository.findTop9ByOrderByViewCountDesc(); // 🔄 인기글 9개로 변경
        model.addAttribute("posts", posts);
        model.addAttribute("popularPosts", popularPosts);
        return "MainPage";
    }

    @GetMapping("/MainPage")
    public String showMainPage(Model model) {
        List<Post> posts = postRepository.findTop10ByOrderByCreatedAtDesc();
        List<Post> popularPosts = postRepository.findTop9ByOrderByViewCountDesc(); // 🔄 동일하게 적용
        model.addAttribute("posts", posts);
        model.addAttribute("popularPosts", popularPosts);
        return "MainPage";
    }
}
