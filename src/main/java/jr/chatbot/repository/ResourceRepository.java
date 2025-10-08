package jr.chatbot.repository;

import jr.chatbot.entity.Resource;
import jr.chatbot.enums.ResourceStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface ResourceRepository<T extends Resource> extends JpaRepository<T, UUID> {
    List<T> findByResourceStatus(ResourceStatusEnum resourceStatus);

    List<T> findByResourceStatusAndOwnerId(ResourceStatusEnum resourceStatus, UUID ownerId);

    List<T> findByOwnerId(UUID ownerId);
}

