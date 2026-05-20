package br.com.autospec.backend.modules.history.service;

import br.com.autospec.backend.core.exception.ResourceNotFoundException;
import br.com.autospec.backend.modules.history.dto.UserHistoryResponseDTO;
import br.com.autospec.backend.modules.history.entity.ActionType;
import br.com.autospec.backend.modules.history.entity.UserHistory;
import br.com.autospec.backend.modules.history.repository.UserHistoryRepository;
import br.com.autospec.backend.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserHistoryService {

    private final UserHistoryRepository historyRepository;

    public void record(User user, ActionType type, String title, String description, Long referenceId) {
        historyRepository.save(UserHistory.builder()
                .user(user)
                .actionType(type)
                .title(title)
                .description(description)
                .referenceId(referenceId)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<UserHistoryResponseDTO> list(User user, ActionType actionType, Pageable pageable) {
        Page<UserHistory> page = (actionType == null)
                ? historyRepository.findByUserAndDeletedFalse(user, pageable)
                : historyRepository.findByUserAndActionTypeAndDeletedFalse(user, actionType, pageable);
        return page.map(h -> new UserHistoryResponseDTO(
                h.getId(), h.getActionType(), h.getTitle(),
                h.getDescription(), h.getReferenceId(), h.getCreatedAt()
        ));
    }

    @Transactional
    public void deleteEntry(User user, Long historyId) {
        UserHistory h = historyRepository.findByIdAndUser(historyId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado"));
        h.setDeleted(true);
        historyRepository.save(h);
    }

    @Transactional
    public void clearAll(User user) {
        historyRepository.softDeleteAllByUser(user);
    }
}
