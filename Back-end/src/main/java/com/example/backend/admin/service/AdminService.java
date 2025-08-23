package com.example.backend.admin.service;

import com.example.backend.admin.dto.response.AdminResponse;
import com.example.backend.board.entity.Board;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.comment.entity.Comment;
import com.example.backend.comment.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public List<AdminResponse.ReportBoardResponse> getReportBoard() {
        List<Board> reportedBoards = boardRepository.findByBoardReportGreaterThanEqual(5);
        return reportedBoards.stream()
                .map(board -> AdminResponse.ReportBoardResponse.builder()
                        .boardId(board.getBoardId())
                        .title(board.getTitle())
                        .content(board.getContent())
                        .writerName(board.getUserId().getUserNickname())
                        .boardReport(board.getBoardReport())
                        .createdAt(board.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public List<AdminResponse.ReportCommentResponse> getReportComment() {
        List<Comment> reportedComments = commentRepository.findByCommentReportGreaterThanEqual(5);
        return reportedComments.stream()
                .map(comment -> AdminResponse.ReportCommentResponse.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .boardId(comment.getBoardId().getBoardId())
                        .commentReport(comment.getCommentReport())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void boardDelete(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        }
        boardRepository.deleteById(boardId);
    }

    @Transactional
    public void commentDelete(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new IllegalArgumentException("해당 댓글을 찾을 수 없습니다.");
        }
        commentRepository.deleteById(commentId);
    }
}
