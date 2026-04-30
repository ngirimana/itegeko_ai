package rw.itegeko.legal.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.itegeko.legal.entities.LegalCategory;

public interface LegalCategoryRepository extends JpaRepository<LegalCategory, UUID> {}
