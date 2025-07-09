package com.example.backend.board.entity;


import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "board_id", columnDefinition = "BINARY(16)")
    private UUID boardId;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "count", nullable = false)
    private int count;

    @Column(name = "board_report", nullable = false)
    private int boardReport;

    @Column(name = "tag", length = 200)
    private String tag;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;


    public void update(String title, String content, String tag, String imageUrl) {
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.imageUrl = imageUrl;
        this.updatedAt = LocalDateTime.now();
    }



    public void updateCount(int newCount) {
        this.count = newCount;
    }

    public void setBoardReport(int boardReport) {
        this.boardReport = boardReport;
    }

}


