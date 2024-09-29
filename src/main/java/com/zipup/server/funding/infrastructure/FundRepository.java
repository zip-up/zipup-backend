package com.zipup.server.funding.infrastructure;

import com.zipup.server.funding.domain.Fund;
import com.zipup.server.funding.dto.FundingAllResponse;
import com.zipup.server.funding.dto.FundingDetailResponse;
import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.user.domain.User;
import org.springframework.data.domain.Pageable;
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

  @Query("SELECT new com.zipup.server.funding.dto.FundingSummaryResponse(" +
          "f.id, " +
          "f.title, " +
          "COALESCE(f.imageUrl, ''), " +
          "DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE), " +
          "CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int), " +
          "u.id " +
          ") " +
          "FROM Fund f " +
          "JOIN f.user u ON u.id = :userId AND u.status = :userStatus " +
          "LEFT JOIN f.presents pre ON pre.status = :presentStatus " +
          "LEFT JOIN pre.payment pay " +
          "WHERE f.status = :fundStatus " +
          "GROUP BY f.id, f.title, f.fundingPeriod.finishFunding, u.id, f.goalPrice " +
          "ORDER BY CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int) DESC "
  )
  List<FundingSummaryResponse> findFundingSummaryByUserIdAndStatus(
          @Param("userId") UUID userId
          , @Param("userStatus") ColumnStatus userStatus
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
  );

  @Query("SELECT new com.zipup.server.funding.dto.FundingSummaryResponse(" +
          "f.id, " +
          "f.title, " +
          "COALESCE(f.imageUrl, ''), " +
          "DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE), " +
          "CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int), " +
          "u.id " +
          ") " +
          "FROM Fund f " +
          "LEFT JOIN f.user u ON u.status = :userStatus " +
          "LEFT JOIN f.presents pre ON pre.status = :presentStatus " +
          "LEFT JOIN pre.payment pay " +
          "WHERE f.status = :fundStatus " +
          "GROUP BY f.id, f.title, f.fundingPeriod.finishFunding, f.goalPrice, u.id " +
          "HAVING DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE) > 0 " +
          "AND SUM(pay.balanceAmount) / f.goalPrice < 1 " +
          "ORDER BY CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int) DESC "
  )
  List<FundingSummaryResponse> findPopularFundingSummaryByStatus(
          @Param("userStatus") ColumnStatus userStatus
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
          , Pageable pageable
  );

  @Query("SELECT new com.zipup.server.funding.dto.FundingDetailResponse(" +
          "f.id, " +
          "f.title, " +
          "f.description, " +
          "COALESCE(f.imageUrl, ''), " +
          "COALESCE(f.productUrl, ''), " +
          "DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE), " +
          "CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int), " +
          "f.goalPrice, " +
          "u.id, " +
          "u.name " +
          ") " +
          "FROM Fund f " +
          "LEFT JOIN f.user u ON u.status = :userStatus " +
          "LEFT JOIN f.presents pre ON pre.status = :presentStatus " +
          "LEFT JOIN pre.payment pay " +
          "WHERE f.id = :fundId AND f.status = :fundStatus " +
          "GROUP BY f.id, f.title, f.description, f.fundingPeriod.finishFunding, u.id, f.goalPrice"
  )
  FundingDetailResponse findFundingDetailByFundIdAndStatus(
          @Param("fundId") UUID fundId
          , @Param("userStatus") ColumnStatus userStatus
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
  );

  @Query("SELECT new com.zipup.server.funding.dto.FundingAllResponse(" +
          "f.id, " +
          "f.title, " +
          "f.description, " +
          "DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE), " +
          "CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int), " +
          "f.goalPrice, " +
          "u " +
          ") " +
          "FROM Fund f " +
          "LEFT JOIN f.user u ON u.status = :userStatus " +
          "LEFT JOIN f.presents pre ON pre.status = :presentStatus " +
          "LEFT JOIN pre.payment pay " +
          "WHERE f.status = :fundStatus " +
          "GROUP BY f.id, f.title, f.description, f.fundingPeriod.finishFunding, u.id, f.goalPrice"
  )
  List<FundingAllResponse> findFundingDetailAll(
          @Param("userStatus") ColumnStatus userStatus
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
  );
}
