package com.example.backend.comment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class CommentRequestDto {
    private String content;
}
