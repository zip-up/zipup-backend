package com.zipup.server.funding.infrastructure;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FundRepository extends JpaRepository<Fund, UUID> {
  List<Fund> findAllByUser(User user);
}
