package com.zipup.server.present.infrastructure;

import com.zipup.server.present.domain.Present;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PresentRepository extends JpaRepository<Present, UUID> {
}
