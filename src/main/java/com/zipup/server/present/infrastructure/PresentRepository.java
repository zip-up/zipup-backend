package com.zipup.server.present.infrastructure;

import com.zipup.server.funding.dto.FundingSummaryResponse;
import com.zipup.server.global.util.entity.ColumnStatus;
import com.zipup.server.payment.domain.Payment;
import com.zipup.server.present.domain.Present;
import com.zipup.server.present.dto.PresentSummaryResponse;
import com.zipup.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresentRepository extends JpaRepository<Present, UUID> {
  List<Present> findAllByUserAndStatus(User user, ColumnStatus status);
  List<Present> findAllByUserAndStatusIsNot(User user, ColumnStatus status);
  Optional<Present> findByPayment(Payment payment);
  @Query("SELECT new com.zipup.server.funding.dto.FundingSummaryResponse(" +
          "f.id, " +
          "f.title, " +
          "COALESCE(f.imageUrl, ''), " +
          "DATEDIFF(f.fundingPeriod.finishFunding, CURRENT_DATE), " +
          "CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int), " +
          "u.id " +
          ") " +
          "FROM Fund f " +
          "LEFT JOIN f.presents pre ON pre.status = :presentStatus " +
          "JOIN pre.user u ON u.id = :userId AND u.status = :userStatus " +
          "LEFT JOIN pre.payment pay " +
          "WHERE f.status = :fundStatus " +
          "GROUP BY f.id, u.id, f.title, u.profileImage " +
          "ORDER BY pay.balanceAmount DESC "
  )
  List<FundingSummaryResponse> findFundingSummaryByUserIdAndStatus(
          @Param("userId") UUID userId
          , @Param("userStatus") ColumnStatus userStatus
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
  );
  @Query("SELECT new com.zipup.server.present.dto.PresentSummaryResponse(" +
          "pre.id, " +
          "pre.senderName, " +
          "CAST(COALESCE(SUM(pay.balanceAmount) / f.goalPrice * 100, 0) AS int), " +
          "COALESCE(u.profileImage, ''), " +
          "u.id, " +
          "f.id, " +
          "pre.congratsMessage, " +
          "pay.id " +
          ") " +
          "FROM Present pre " +
          "JOIN pre.fund f ON f.id = :fundId AND f.status = :fundStatus " +
          "LEFT JOIN pre.payment pay " +
          "LEFT JOIN pre.user u ON u.status = :userStatus " +
          "WHERE pre.status = :presentStatus " +
          "GROUP BY pre.id, pre.senderName, f.id, pre.congratsMessage " +
          "ORDER BY pay.balanceAmount DESC "
  )
  List<PresentSummaryResponse> findPresentSummaryByFundIdAndStatus(
          @Param("fundId") UUID fundId
          , @Param("userStatus") ColumnStatus userStatus
          , @Param("presentStatus") ColumnStatus presentStatus
          , @Param("fundStatus") ColumnStatus fundStatus
  );
}
