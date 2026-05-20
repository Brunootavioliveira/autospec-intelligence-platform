package br.com.autospec.backend.modules.history.repository;

import br.com.autospec.backend.modules.history.entity.ActionType;
import br.com.autospec.backend.modules.history.entity.UserHistory;
import br.com.autospec.backend.modules.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    Page<UserHistory> findByUserAndDeletedFalse(User user, Pageable pageable);
    Page<UserHistory> findByUserAndActionTypeAndDeletedFalse(User user, ActionType actionType, Pageable pageable);
    Optional<UserHistory> findByIdAndUser(Long id, User user);

    @Modifying
    @Transactional
    @Query("UPDATE UserHistory h SET h.deleted = true WHERE h.user = :user")
    void softDeleteAllByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserHistory h WHERE h.deleted = true AND h.createdAt < :cutoff")
    int hardDeleteOldSoftDeleted(@Param("cutoff") java.time.LocalDateTime cutoff);
}
