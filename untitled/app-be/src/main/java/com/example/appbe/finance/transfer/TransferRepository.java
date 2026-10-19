package com.example.appbe.finance.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<MoneyTransfer, Long> {
    Optional<MoneyTransfer> findByRequestIdAndUserId(String requestId, Long userId);
    List<MoneyTransfer> findByUserIdOrderByDateDescIdDesc(Long userId);
    @Query("""
            select coalesce(sum(case when t.sourceAccount.id = :accountId then -t.amount else t.amount end), 0)
            from MoneyTransfer t where t.user.id = :userId and t.date >= :from
              and (t.sourceAccount.id = :accountId or t.destinationAccount.id = :accountId)
            """)
    BigDecimal sumCashImpact(@Param("accountId") Long accountId, @Param("userId") Long userId, @Param("from") LocalDate from);
    @Query("""
            select count(t) from MoneyTransfer t
            where t.sourceAccount.id = :accountId or t.destinationAccount.id = :accountId
            """)
    long countForAccount(@Param("accountId") Long accountId);
}
