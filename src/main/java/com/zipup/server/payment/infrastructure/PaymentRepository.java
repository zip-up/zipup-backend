package com.zipup.server.payment.infrastructure;

import com.zipup.server.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByPaymentKey(String paymentKey);
  Boolean existsByPaymentKey(String paymentKey);
  Boolean existsByOrderId(String orderId);
}
