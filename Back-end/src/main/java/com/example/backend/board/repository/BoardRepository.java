package com.example.backend.board.repository;

import com.example.backend.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByBoardReportGreaterThanEqual(int boardReport);
}

