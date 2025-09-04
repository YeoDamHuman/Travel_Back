package com.example.backend.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class BoardCreateResponseDto {
    private UUID boardId;
    private UUID scheduleId;
}

