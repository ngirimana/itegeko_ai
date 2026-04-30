package rw.itegeko.legal.repositories;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.itegeko.legal.entities.LegalSource;

public interface LegalSourceRepository extends JpaRepository<LegalSource, UUID> {}
