package com.fishtripplanner.repository;

import com.fishtripplanner.domain.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    // 조회수 기준 상위 인기글 5개 가져오기
    List<Post> findTop5ByOrderByViewCountDesc();
}
