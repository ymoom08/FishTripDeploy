package com.fishtripplanner.controller.board;

import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.repository.PostRepository;
import com.fishtripplanner.service.FileUploadService;
import lombok.RequiredArgsConstructor;
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

    // 게시글 목록 + 검색
    @GetMapping
    public String list(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Post> posts = (keyword != null && !keyword.isEmpty())
                ? postRepository.findByTitleContainingOrContentContaining(keyword, keyword)
                : postRepository.findAll();
        model.addAttribute("posts", posts);
        return "board/list";
    }

    // 글 작성 폼
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("post", new Post());
        return "board/write";
    }

    // 글 저장
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
        postRepository.save(post);
        return "redirect:/posts";
    }

    // 상세보기
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));
        model.addAttribute("post", post);
        return "board/view";
    }

    // 수정 폼
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));
        model.addAttribute("post", post);
        return "board/edit";
    }

    // 수정 저장
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Post updatedPost) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));
        post.setTitle(updatedPost.getTitle());
        post.setContent(updatedPost.getContent());
        postRepository.save(post);
        return "redirect:/posts";
    }

    // 삭제
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        postRepository.deleteById(id);
        return "redirect:/posts";
    }
}
