package br.com.autospec.backend.modules.auth.entity;

import br.com.autospec.backend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Instant expiryDate;
}
