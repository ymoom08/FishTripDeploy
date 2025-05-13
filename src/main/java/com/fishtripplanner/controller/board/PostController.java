package com.fishtripplanner.controller.board;

import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.repository.PostRepository;
import com.fishtripplanner.security.CustomOAuth2User;
import com.fishtripplanner.security.CustomUserDetails;
import com.fishtripplanner.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final FileUploadService fileUploadService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, 12);
        Page<Post> posts = (keyword != null && !keyword.isEmpty())
                ? postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
                : postRepository.findAll(pageable);

        List<Post> popularPosts = postRepository.findTop5ByOrderByViewCountDesc();

        model.addAttribute("posts", posts);
        model.addAttribute("popularPosts", popularPosts);
        return "board/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("post", new Post());
        return "board/write";
    }

    @PostMapping
    public String save(@ModelAttribute Post post,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                       @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile) {

        if (imageFile != null && !imageFile.isEmpty()) {
            post.setImagePath(fileUploadService.upload(imageFile));
        }
        if (videoFile != null && !videoFile.isEmpty()) {
            post.setVideoPath(fileUploadService.upload(videoFile));
        }
        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            post.setProfileImagePath(fileUploadService.upload(profileImageFile));
        }

        post.setViewCount(0);
        postRepository.save(post);
        return "redirect:/posts";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal Object principal) {
        //밑 조건문은 세션에 저장되었는지 확인하는 디버그 용도임.
        if (principal instanceof CustomUserDetails userDetails) {
            System.out.println("🧍 일반 로그인 사용자: " + userDetails.getUsername());
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            System.out.println("🧍‍♂️ 소셜 로그인 사용자: " + oauthUser.getUser().getUsername());
        } else {
            System.out.println("⚠️ 로그인 정보 없음 또는 알 수 없는 타입");
        }
        //여기까지 디버그 용도임 추후에 지울거임.
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1); // 조회수 증가
        postRepository.save(post);

        model.addAttribute("post", post);
        return "board/view";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));
        model.addAttribute("post", post);
        return "board/edit";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, @ModelAttribute Post updatedPost) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));

        post.setTitle(updatedPost.getTitle());
        post.setContent(updatedPost.getContent());
        postRepository.save(post);
        return "redirect:/posts";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        postRepository.deleteById(id);
        return "redirect:/posts";
    }
}
