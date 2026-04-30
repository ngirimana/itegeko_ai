package rw.itegeko.identity.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.itegeko.identity.entities.UserActivity;

public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {
    List<UserActivity> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}
