package com.fishtripplanner.controller.board;

import com.fishtripplanner.domain.User;
import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.domain.comment.Comment;
import com.fishtripplanner.repository.PostRepository;
import com.fishtripplanner.repository.CommentRepository;
import com.fishtripplanner.security.CustomOAuth2User;
import com.fishtripplanner.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    private static final String UPLOAD_DIR = "uploads";

    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, 12);
        Page<Post> posts = (keyword != null && !keyword.isEmpty())
                ? postRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable)
                : postRepository.findAll(pageable);

        List<Post> popularPosts = postRepository.findTop9ByOrderByViewCountDesc();

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
                       @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                       @AuthenticationPrincipal Object principal) {

        User loggedInUser = extractUserFromPrincipal(principal);
        if (loggedInUser == null || loggedInUser.getId() == null) {
            throw new IllegalStateException("로그인된 사용자 정보가 없거나 식별자가 없습니다.");
        }

        post.setWriter(loggedInUser.getNickname());
        post.setUser(loggedInUser);
        post.setViewCount(0);

        try {
            if (profileImageFile != null && !profileImageFile.isEmpty()) {
                post.setProfileImagePath(storeFile(profileImageFile));
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                post.setImagePath(storeFile(imageFile));
            }
            if (videoFile != null && !videoFile.isEmpty()) {
                post.setVideoPath(storeFile(videoFile));
            }
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류 발생", e);
        }

        postRepository.save(post);
        return "redirect:/posts";
    }

    private String storeFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFilename = UUID.randomUUID().toString() + extension;

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        File savedFile = new File(dir, storedFilename);
        file.transferTo(savedFile);

        return "/uploads/" + storedFilename; // view.html에서 사용되는 경로
    }

    @Transactional
    @GetMapping("/{id}")
    public String view(@PathVariable("id") Long id, Model model,
                       @AuthenticationPrincipal Object principal) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1);

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);

        if (principal instanceof CustomUserDetails userDetails) {
            System.out.println("🧍 일반 로그인 사용자: " + userDetails.getUsername());
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            System.out.println("🧍‍♂️ 소셜 로그인 사용자: " + oauthUser.getUser().getUsername());
        } else {
            System.out.println("⚠️ 로그인 정보 없음 또는 알 수 없는 타입");
        }

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

    private User extractUserFromPrincipal(Object principal) {
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        } else if (principal instanceof CustomOAuth2User oauthUser) {
            return oauthUser.getUser();
        }
        return null;
    }
}
