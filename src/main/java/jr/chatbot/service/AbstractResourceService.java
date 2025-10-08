package jr.chatbot.service;

import jr.chatbot.entity.Resource;
import jr.chatbot.enums.ResourceStatusEnum;
import jr.chatbot.util.SecurityUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractResourceService<T extends Resource, R extends JpaRepository<T, UUID>> {

    protected final R repository;
    private final String resourceName;

    protected AbstractResourceService(R repository) {
        this.repository = repository;
        this.resourceName = deriveResourceName();
    }

    private String deriveResourceName() {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
            Class<?> resourceClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            return resourceClass.getSimpleName();
        } catch (Exception e) {
            return "Resource";
        }
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public Optional<T> findById(UUID id) {
        return repository.findById(id);
    }

    public T findByIdOrThrow(UUID id) {
        return findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, resourceName + " not found"));
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
        entity.setId(existing.getId());
        entity.setOwnerId(existing.getOwnerId());
        entity.setCreatedAt(existing.getCreatedAt());
        entity.setResourceStatus(existing.getResourceStatus());

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

    protected String getResourceName() {
        return resourceName;
    }
}
