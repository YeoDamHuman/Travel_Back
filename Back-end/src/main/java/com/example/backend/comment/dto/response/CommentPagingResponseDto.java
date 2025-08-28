package com.example.backend.comment.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CommentPagingResponseDto {
    private List<CommentResponseDto> comments;
    private boolean hasNextComment;
}

