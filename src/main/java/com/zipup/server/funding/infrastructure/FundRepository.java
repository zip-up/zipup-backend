package com.zipup.server.funding.infrastructure;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FundRepository extends JpaRepository<Fund, UUID> {
  List<Fund> findAllByUserAndStatus(User user, ColumnStatus status);
  List<Fund> findAllByStatusAfterAndFundingPeriod_FinishFunding(ColumnStatus status, LocalDateTime currentDate);
  List<Fund> findAllByStatus(ColumnStatus status);
  @Query("SELECT new com.zipup.server.funding.dto.FundingSummaryResponse(" +
            "f.id, " +
            "f.title, " +
            "f.imageUrl, " +
            "DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE) , " +
            "CAST(COALESCE(SUM(p.balanceAmount) / f.goalPrice * 100, 0) AS int) , " +
            "f.user.id " +
          ") " +
          "FROM Fund f " +
          "LEFT JOIN f.presents pre ON pre.status = :presentStatus " +
          "LEFT JOIN pre.payment p " +
          "WHERE f.user.id = :userId AND f.status = :fundStatus " +
          "GROUP BY f.id, f.title, f.fundingPeriod.finishFunding, f.user.id, f.goalPrice")
  List<FundingSummaryResponse> findFundingSummaryByUserIdAndStatus(
          @Param("userId") UUID userId
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
  );
}
