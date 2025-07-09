package com.example.backend.board.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardUpdateRequestDto {
    private String title;
    private String content;
    private String tag;
    private String imageUrl;
}
