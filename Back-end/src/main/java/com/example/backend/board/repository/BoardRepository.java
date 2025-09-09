package com.example.backend.board.repository;

import com.example.backend.board.entity.Board;
import com.example.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByBoardReportGreaterThanEqual(int boardReport);
    
    List<Board> findByUserId(User user);
}

