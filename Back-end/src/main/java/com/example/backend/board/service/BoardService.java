package com.example.backend.board.service;

import com.example.backend.board.dto.request.BoardRequestDto;
import com.example.backend.board.dto.request.BoardUpdateRequestDto;
import com.example.backend.board.dto.response.BoardCreateResponseDto;
import com.example.backend.board.dto.response.BoardDetailResponseDto;
import com.example.backend.board.entity.Board;
import com.example.backend.board.entity.BoardImage;
import com.example.backend.comment.dto.response.CommentResponseDto;
import com.example.backend.comment.entity.Comment;
import com.example.backend.comment.repository.CommentRepository;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.board.dto.response.BoardListResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    public BoardCreateResponseDto createBoard(BoardRequestDto requestDto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(requestDto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다."));

        Board board = Board.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .tag(requestDto.getTag())
                .userId(user)
                .schedule(schedule)
                .build();

        if (requestDto.getImageUrls() != null) {
            requestDto.getImageUrls().forEach(url ->
                    board.addImage(BoardImage.builder().imgUrl(url).build())
            );
        }

        Board saved = boardRepository.save(board);

        return BoardCreateResponseDto.builder()
                .boardId(saved.getBoardId())
                .scheduleId(saved.getSchedule().getScheduleId())
                .build();
    }

    public List<BoardListResponseDto> getBoardList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Board> boards = boardRepository.findAll(pageable);

        return boards.stream().map(board -> {
            List<String> thumbs = board.getImages().stream()
                    .limit(3)
                    .map(BoardImage::getImgUrl)
                    .toList();

            return BoardListResponseDto.builder()
                    .boardId(board.getBoardId())
                    .title(board.getTitle())
                    .userNickname(board.getUserId().getUserNickname())
                    .userProfileImage(board.getUserId().getUserProfileImage())
                    .createdAt(board.getCreatedAt())
                    .count(board.getCount())
                    .tag(board.getTag())
                    .imageUrls(thumbs)
                    .build();
        }).toList();
    }

    //상세 조회
    @Transactional(readOnly = true)
    public BoardDetailResponseDto getBoardDetail(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        List<String> images = board.getImages().stream()
                .map(BoardImage::getImgUrl)
                .toList();


        // 댓글 페이징 조회 (처음 5개만)
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findByBoardId(board, pageable);


        List<CommentResponseDto> comments = commentPage.stream()
                .map(comment -> CommentResponseDto.builder()
                        .commentId(comment.getCommentId())
                        .userNickname(comment.getUserId().getUserNickname())
                        .userProfileImage(comment.getUserId().getUserProfileImage())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();

        return BoardDetailResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .userNickname(board.getUserId().getUserNickname())
                .userProfileImage(board.getUserId().getUserProfileImage())
                .createdAt(board.getCreatedAt())
                .count(board.getCount())
                .imageUrls(images)
                .tag(board.getTag())
                .comments(comments)
                .hasNextComment(commentPage.hasNext())
                .build();
    }


    //수정
    @Transactional
    public void updateBoard(UUID boardId, UUID userId, BoardUpdateRequestDto requestDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getUserId().getUserId().equals(userId)) {
            throw new SecurityException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        board.update(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getTag()
        );

        List<String> newUrls = requestDto.getImageUrls() == null ? List.of() : requestDto.getImageUrls();

        board.getImages().removeIf(img -> !newUrls.contains(img.getImgUrl()));

        for (String url : newUrls) {
            boolean exists = board.getImages().stream().anyMatch(i -> i.getImgUrl().equals(url));
            if (!exists) board.addImage(BoardImage.builder().imgUrl(url).build());
        }
    }

    //삭제
    @Transactional
    public void deleteBoard(UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        if (!board.getUserId().getUserId().equals(userId)) {
            throw new SecurityException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        boardRepository.delete(board);
    }

    //신고
    @Transactional
    public void reportBoard(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        board.setBoardReport(board.getBoardReport() + 1);  // 현재 신고수 + 1
    }

}
