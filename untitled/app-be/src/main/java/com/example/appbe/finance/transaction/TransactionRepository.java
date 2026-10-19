package com.example.appbe.finance.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findTop10ByUserIdOrderByTransactionDateDescCreatedAtDesc(Long userId);

    List<Transaction> findByUserIdOrderByTransactionDateDescCreatedAtDesc(Long userId);

    List<Transaction> findByUserIdAndFinancialAccountIdOrderByTransactionDateDescCreatedAtDesc(
            Long userId,
            Long financialAccountId
    );

    @Query("""
            select coalesce(sum(
                case
                    when transaction.type = :incomeType then transaction.amount
                    when transaction.type = :expenseType then -transaction.amount
                    else 0
                end
            ), 0)
            from Transaction transaction
            where transaction.user.id = :userId
              and transaction.financialAccount.id = :accountId
              and transaction.transactionDate >= :from
            """)
    BigDecimal sumBalanceImpact(
            @Param("userId") Long userId,
            @Param("accountId") Long accountId,
            @Param("from") LocalDate from,
            @Param("incomeType") TransactionType incomeType,
            @Param("expenseType") TransactionType expenseType
    );

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
