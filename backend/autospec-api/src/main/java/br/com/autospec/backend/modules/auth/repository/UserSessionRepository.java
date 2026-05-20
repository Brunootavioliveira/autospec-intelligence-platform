package br.com.autospec.backend.modules.auth.repository;

import br.com.autospec.backend.modules.auth.entity.UserSession;
import br.com.autospec.backend.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserAndActiveTrue(User user);

    Optional<UserSession> findBySessionToken(String sessionToken);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user = :user")
    void deactivateAllByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserSession s WHERE s.active = false AND s.lastActive < :cutoff")
    int deleteInactiveOlderThan(@Param("cutoff") java.time.LocalDateTime cutoff);
}
