package com.example.appbe.domain.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTop10ByUserIdOrderByTransactionDateDescCreatedAtDesc(Long userId);

    List<Transaction> findByUserIdAndFinancialAccountIdOrderByTransactionDateDesc(Long userId, Long financialAccountId);

    @Query("""
            select coalesce(sum(transaction.amount), 0)
            from Transaction transaction
            where transaction.user.id = :userId
              and transaction.type = :type
              and transaction.transactionDate between :from and :to
            """)
    BigDecimal sumAmountByUserIdAndTypeBetweenDates(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
