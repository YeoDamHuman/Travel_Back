package com.example.backend.comment.repository;

import com.example.backend.board.entity.Board;
import com.example.backend.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Page<Comment> findByBoardId(Board board, Pageable pageable);

    List<Comment> findByCommentReportGreaterThanEqual(int i);

}
