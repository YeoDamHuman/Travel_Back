package com.example.backend.board.controller;

import com.example.backend.board.dto.request.BoardRequestDto;
import com.example.backend.board.dto.request.BoardUpdateRequestDto;
import com.example.backend.board.dto.response.BoardDetailResponseDto;
import com.example.backend.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.board.dto.response.BoardListResponseDto;
import com.example.backend.common.auth.AuthUtil;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //작성
    @PostMapping
    public ResponseEntity<?> createBoard(@RequestBody BoardRequestDto requestDto) {
        UUID userId = AuthUtil.getCurrentUserId();
        UUID boardId = boardService.createBoard(requestDto, userId);
        return ResponseEntity.ok().body("게시글 작성 완료, boardId: " + boardId);
    }

    //목록 조회
    @GetMapping
    public ResponseEntity<List<BoardListResponseDto>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<BoardListResponseDto> boardList = boardService.getBoardList(page, size);
        return ResponseEntity.ok(boardList);
    }

    //상세 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponseDto> getBoardDetail(@PathVariable UUID boardId) {
        BoardDetailResponseDto detail = boardService.getBoardDetail(boardId);
        return ResponseEntity.ok(detail);
    }

    //수정
    @PutMapping("/{boardId}")
    public ResponseEntity<String> updateBoard(@PathVariable UUID boardId,
                                              @RequestBody BoardUpdateRequestDto requestDto) {
        UUID userId = AuthUtil.getCurrentUserId();
        boardService.updateBoard(boardId, userId, requestDto);
        return ResponseEntity.ok("게시글이 수정되었습니다.");
    }

    //삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(@PathVariable UUID boardId) {
        UUID userId = AuthUtil.getCurrentUserId();
        boardService.deleteBoard(boardId, userId);
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    //신고
    @PatchMapping("/{boardId}/report")
    public ResponseEntity<String> reportBoard(@PathVariable UUID boardId) {
        boardService.reportBoard(boardId);
        return ResponseEntity.ok("게시글을 신고했습니다.");
    }

}
