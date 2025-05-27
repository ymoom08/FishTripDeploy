package com.fishtripplanner.repository;

import com.fishtripplanner.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    List<Post> findTop9ByOrderByViewCountDesc();      // 인기 게시글 상위 9개
    List<Post> findTop10ByOrderByCreatedAtDesc();     // 최신 게시글 상위 10개
    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);
}
