package com.example.backend.board.dto.response;

import com.example.backend.comment.dto.response.CommentResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class BoardDetailResponseDto {
    private UUID boardId;
    private String title;
    private String content;
    private String userNickname;
    private String userProfileImage;
    private LocalDateTime createdAt;
    private int count;
    private String tag;
    private List<CommentResponseDto> comments;
    private String imageUrl;
    private boolean hasNextComment;

}

