package com.example.backend.comment.controller;

import com.example.backend.board.dto.request.BoardUpdateRequestDto;
import com.example.backend.comment.dto.request.CommentRequestDto;
import com.example.backend.comment.dto.request.CommentUpdateRequestDto;
import com.example.backend.comment.dto.response.CommentPagingResponseDto;
import com.example.backend.comment.service.CommentService;
import com.example.backend.common.auth.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    //작성
    @PostMapping("/{boardId}")
    public ResponseEntity<UUID> createComment(
            @PathVariable UUID boardId,
            @RequestBody CommentRequestDto requestDto
    ) {
        UUID userId = AuthUtil.getCurrentUserId();
        UUID commentId = commentService.createComment(boardId, userId, requestDto);
        return ResponseEntity.ok(commentId);
    }

    //수정
    @PutMapping("/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable UUID commentId,
                                              @RequestBody CommentUpdateRequestDto requestDto) {
        UUID userId = AuthUtil.getCurrentUserId();
        commentService.updateComment(commentId, userId, requestDto);
        return ResponseEntity.ok("댓글이 수정되었습니다.");
    }

    //삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable UUID commentId) {
        UUID userId = AuthUtil.getCurrentUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다.");
    }

    //신고
    @PatchMapping("/{commentId}/report")
    public ResponseEntity<String> reportComment(@PathVariable UUID commentId) {
        commentService.reportComment(commentId);
        return ResponseEntity.ok("댓글을 신고했습니다.");
    }

    //댓글 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<CommentPagingResponseDto> getCommentsByBoard(
            @PathVariable UUID boardId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        CommentPagingResponseDto responseDto = commentService.getComments(boardId, page, size);
        return ResponseEntity.ok(responseDto);
    }

}
