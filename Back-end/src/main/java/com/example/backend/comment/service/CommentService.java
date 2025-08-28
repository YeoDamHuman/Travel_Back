package com.example.backend.comment.service;

import com.example.backend.board.entity.Board;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.comment.dto.request.CommentRequestDto;
import com.example.backend.comment.dto.request.CommentUpdateRequestDto;
import com.example.backend.comment.dto.response.CommentResponseDto;
import com.example.backend.comment.dto.response.CommentPagingResponseDto;
import com.example.backend.comment.entity.Comment;
import com.example.backend.comment.repository.CommentRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    //작성
    @Transactional
    public UUID createComment(UUID boardId, UUID userId, CommentRequestDto requestDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .boardId(board)
                .userId(user)
                .build();

        return commentRepository.save(comment).getCommentId();
    }

    //수정
    @Transactional
    public void updateComment(UUID commentId, UUID userId, CommentUpdateRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getUserId().getUserId().equals(userId)) {
            throw new SecurityException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.update(
                requestDto.getContent()
        );
    }

    //삭제
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!comment.getUserId().getUserId().equals(userId)) {
            throw new SecurityException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }

    //신고
    @Transactional
    public void reportComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        comment.setCommentReport(comment.getCommentReport() + 1);  // 현재 신고수 + 1
    }

    //더보기 조회
    @Transactional(readOnly = true)
    public CommentPagingResponseDto getComments(UUID boardId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        Page<Comment> commentPage = commentRepository.findByBoardId(board, pageable);

        List<CommentResponseDto> commentDtos = commentPage.stream()
                .map(comment -> CommentResponseDto.builder()
                        .commentId(comment.getCommentId())
                        .userNickname(comment.getUserId().getUserNickname())
                        .userProfileImage(comment.getUserId().getUserProfileImage())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();

        return CommentPagingResponseDto.builder()
                .comments(commentDtos)
                .hasNextComment(commentPage.hasNext())
                .build();
    }


}
