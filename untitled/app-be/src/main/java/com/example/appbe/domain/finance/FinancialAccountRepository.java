package com.example.appbe.domain.finance;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {

    List<FinancialAccount> findByUserIdAndActiveTrueOrderByNameAsc(Long userId);

    List<FinancialAccount> findByUserIdAndType(Long userId, FinancialAccountType type);

    @Query("""
            select coalesce(sum(account.currentBalance), 0)
            from FinancialAccount account
            where account.user.id = :userId and account.active = true
            """)
    BigDecimal sumActiveBalancesByUserId(@Param("userId") Long userId);
}
