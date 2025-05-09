package com.fishtripplanner.controller.board;

import com.fishtripplanner.domain.comment.Comment;
import com.fishtripplanner.domain.board.Post;
import com.fishtripplanner.domain.User;
import com.fishtripplanner.repository.CommentRepository;
import com.fishtripplanner.repository.PostRepository;
import com.fishtripplanner.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @PostMapping("/add")
    @Transactional
    public String addComment(@RequestParam("postId") Long postId,
                             @RequestParam("writer") String writer,
                             @RequestParam("content") String content) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));

        User user = userRepository.findByNickname(writer)
                .orElseThrow(() -> new IllegalArgumentException("작성자에 해당하는 유저가 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .writer(writer)
                .content(content)
                .post(post)
                .user(user)
                .build();

        commentRepository.save(comment);
        return "redirect:/posts/" + postId;
    }

    @PostMapping("/add-ajax")
    @ResponseBody
    @Transactional
    public List<Comment> addCommentAjax(@RequestParam("postId") Long postId,
                                        @RequestParam("writer") String writer,
                                        @RequestParam("content") String content) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글"));

        User user = userRepository.findByNickname(writer)
                .orElseThrow(() -> new IllegalArgumentException("작성자에 해당하는 유저가 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .writer(writer)
                .content(content)
                .post(post)
                .user(user)
                .build();

        commentRepository.save(comment);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    @GetMapping("/post/{postId}")
    @ResponseBody
    public List<Comment> getCommentsByPost(@PathVariable("postId") Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
}
