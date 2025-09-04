package com.example.backend.board.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class BoardRequestDto {
    private String title;
    private String content;
    private String tag;
    private List<String> imageUrls;
    private UUID scheduleId;
}
