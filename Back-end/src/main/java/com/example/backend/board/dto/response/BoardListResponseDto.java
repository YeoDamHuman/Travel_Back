package com.example.backend.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class BoardListResponseDto {
    private UUID boardId;
    private String title;
    private String userNickname;
    private LocalDateTime createdAt;
    private int count;
    private String tag;
    private String imageUrl;
}

