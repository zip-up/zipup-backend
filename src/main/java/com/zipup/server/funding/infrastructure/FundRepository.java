package com.zipup.server.funding.infrastructure;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FundRepository extends JpaRepository<Fund, UUID> {
  List<Fund> findAllByUserAndStatus(User user, ColumnStatus status);
  @Query(value = "SELECT fund_id AS id, title, image_url, status, " +
          "TIMESTAMPDIFF(day, now(), finish_funding) AS d_day " +
          "FROM fundings " +
          "WHERE status = :status " +
          "HAVING d_day > 0 " +
          "ORDER BY d_day",
          nativeQuery = true)
  List<FundingSummaryResponse> findPopularFundingByStatus(@Param("status") String status);
}
