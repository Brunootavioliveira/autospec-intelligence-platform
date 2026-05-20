package br.com.autospec.backend.modules.auth.entity;

import br.com.autospec.backend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String sessionToken;

    @Column(length = 100)
    private String deviceInfo;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 100)
    private String browserApp;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastActive;

    @Column(nullable = false)
    private boolean active;
}
