package com.example.backend.comment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder

public class CommentUpdateRequestDto {
    private String content;
}
