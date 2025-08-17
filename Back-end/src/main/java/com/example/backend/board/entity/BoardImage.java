package com.example.backend.board.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "board_img")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "board_img", columnDefinition = "BINARY(16)")
    private UUID boardImg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, columnDefinition = "BINARY(16)")
    private Board board;

    @Column(name = "img_url", nullable = false, length = 500)
    private String imgUrl;

    public void setBoard(Board board) { this.board = board; }
}
