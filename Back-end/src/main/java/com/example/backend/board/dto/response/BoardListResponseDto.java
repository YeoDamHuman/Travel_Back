package com.example.backend.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class BoardListResponseDto {
    private UUID boardId;
    private String title;
    private String userNickname;
    private String userProfileImage;
    private LocalDateTime createdAt;
    private int count;
    private String tag;
    private UUID scheduleId;
    private List<String> imageUrls;
}

