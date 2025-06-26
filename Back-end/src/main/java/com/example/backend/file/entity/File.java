package com.example.backend.file.entity;

import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "File")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "file_id", columnDefinition = "BINARY(16)")
    private UUID fileId;

    @Column(name = "file_name", nullable = true, length = 50)
    private String fileName;

    @Column(name = "file_url", nullable = true, length = 1000)
    private String fileUrl;

    @UpdateTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;
}
