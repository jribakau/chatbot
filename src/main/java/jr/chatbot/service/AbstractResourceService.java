package jr.chatbot.service;

import jr.chatbot.entity.Resource;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.util.SecurityUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractResourceService<T extends Resource, R extends JpaRepository<T, UUID>> {

    protected final R repository;

    protected AbstractResourceService(R repository) {
        this.repository = repository;
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public Optional<T> findById(UUID id) {
        return repository.findById(id);
    }

    public T findByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, getResourceName() + " not found"));
    }

    public T save(T entity) {
        if (entity.getId() == null && entity.getOwnerId() == null) {
            UUID currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null) {
                entity.setOwnerId(currentUserId);
            }
        }
        return repository.save(entity);
    }

    public T update(UUID id, T entity) {
        T existing = findByIdOrThrow(id);
        validateOwnership(existing);
        entity.setId(id);
        entity.setOwnerId(existing.getOwnerId());
        return repository.save(entity);
    }

    public boolean softDelete(UUID id) {
        Optional<T> entityOpt = repository.findById(id);
        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();
            validateOwnership(entity);
            entity.setResourceStatus(ResourceStatusEnum.DELETED);
            repository.save(entity);
            return true;
        }
        return false;
    }

    public void hardDelete(UUID id) {
        T entity = findByIdOrThrow(id);
        validateOwnership(entity);
        repository.delete(entity);
    }

    public void validateOwnership(T entity) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        if (!SecurityUtil.isCurrentUserAdmin() && !entity.getOwnerId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    public void ensureAuthenticated() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
    }

    public UUID getCurrentUserIdOrThrow() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return currentUserId;
    }

    protected abstract String getResourceName();
}
