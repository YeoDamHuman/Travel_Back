package com.example.backend.admin.controller;

import com.example.backend.admin.dto.response.AdminResponse;
import com.example.backend.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/admin")
@Tag(name = "AdminAPI", description = "Admin관련 데이터 불러오는 API")
public class AdminController {
    private final AdminService adminService;


    @GetMapping("/board/report")
    @Operation(summary = "신고된 게시글 리스트 조회", description = "신고된 게시글(Board)에 대한 리스트를 조회하여 관리자에게 반환한다.")
    public ResponseEntity<List<AdminResponse.ReportBoardResponse>> boadReport() {
        List<AdminResponse.ReportBoardResponse> reportBoards = adminService.getReportBoard();
        return ResponseEntity.ok(reportBoards);
    }

    @GetMapping("/comment/report")
    @Operation(summary = "신고된 댓글 리스트 조회", description = "신고된 댓글(Comment)에 대한 리스트를 조회하여 관리자에게 반환한다.")
    public ResponseEntity<List<AdminResponse.ReportCommentResponse>> commentReport() {
        List<AdminResponse.ReportCommentResponse> reportComment = adminService.getReportComment();
        return ResponseEntity.ok(reportComment);
    }

    @DeleteMapping("/board/delete/{boardId}")
    @Operation(summary = "신고된 게시글 삭제", description = "신고된 게시글을 삭제한다.")
    public ResponseEntity<Void> boardDelete(@PathVariable UUID boardId) {
        adminService.boardDelete(boardId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comment/delete/{commentId}")
    @Operation(summary = "신고된 댓글 삭제", description = "신고된 댓글을 삭제한다.")
    public ResponseEntity<Void> commentDelete(@PathVariable UUID commentId) {
        adminService.commentDelete(commentId);
        return ResponseEntity.noContent().build();
    }
}
