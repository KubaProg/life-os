package com.example.appbe.finance.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TransferRepository extends JpaRepository<MoneyTransfer, Long> {
    @Query("""
            select coalesce(sum(case when s.id = :accountId then -t.amount else t.amount end), 0)
            from MoneyTransfer t left join t.sourceAccount s left join t.destinationAccount d
            where t.user.id = :userId and (s.id = :accountId or d.id = :accountId)
            """)
    BigDecimal sumCashBalance(@Param("accountId") Long accountId, @Param("userId") Long userId);
    Optional<MoneyTransfer> findByRequestIdAndUserId(String requestId, Long userId);
    List<MoneyTransfer> findByUserIdOrderByDateDescIdDesc(Long userId);
    @Query("""
            select coalesce(sum(case when s.id = :accountId then -t.amount else t.amount end), 0)
            from MoneyTransfer t left join t.sourceAccount s left join t.destinationAccount d
            where t.user.id = :userId and t.date >= :from
              and (s.id = :accountId or d.id = :accountId)
            """)
    BigDecimal sumCashImpact(@Param("accountId") Long accountId, @Param("userId") Long userId, @Param("from") LocalDate from);
    @Query("""
            select count(t) from MoneyTransfer t
            left join t.sourceAccount s left join t.destinationAccount d
            where s.id = :accountId or d.id = :accountId
            """)
    long countForAccount(@Param("accountId") Long accountId);
}
