package com.zipup.server.present.infrastructure;

import com.zipup.server.present.domain.Present;
import com.zipup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PresentRepository extends JpaRepository<Present, UUID> {
  List<Present> findAllByUser(User user);
}
