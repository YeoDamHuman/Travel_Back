package com.example.backend.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AdminResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReportBoardResponse {
        private UUID boardId;
        private String title;
        private String content;
        private String writerName;
        private int boardReport;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReportCommentResponse {
        private UUID commentId;
        private String content;
        private int commentReport;
        private UUID boardId;
        private LocalDateTime createdAt;
    }

}
