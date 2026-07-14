package com.example.data_enrichment_service.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private UUID messageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "result", nullable = false)
    private boolean result;

    @Column(name = "event_time", nullable = false, updatable = false)
    private LocalDateTime eventTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
