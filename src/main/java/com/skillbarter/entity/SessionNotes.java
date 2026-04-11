package com.skillbarter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Session replay notes — key takeaways after session completion.
 */
@Entity
@Table(name = "session_notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class SessionNotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @Column(length = 2000)
    private String teacherNotes;

    @Column(length = 2000)
    private String learnerNotes;

    @Column(length = 1000)
    private String keyTakeaways;

    @Column(length = 1000)
    private String homework;

    @Column(length = 1000)
    private String resourcesShared;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
