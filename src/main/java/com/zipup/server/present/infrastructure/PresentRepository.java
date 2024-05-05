package com.zipup.server.present.infrastructure;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.present.domain.Present;
import com.zipup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresentRepository extends JpaRepository<Present, UUID> {
  List<Present> findAllByUserAndStatus(User user, ColumnStatus status);
  Optional<Present> findByUserAndFund(User user, Fund fund);
}
