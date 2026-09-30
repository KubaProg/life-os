package com.example.appbe.finance.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {

    List<FinancialAccount> findByActiveTrueOrderByNameAsc();

    Optional<FinancialAccount> findByIdAndActiveTrue(Long id);

    List<FinancialAccount> findByUserIdAndActiveTrueOrderByNameAsc(Long userId);

    List<FinancialAccount> findByUserIdAndType(Long userId, FinancialAccountType type);

    @Query("""
            select coalesce(sum(account.openingBalance), 0)
            from FinancialAccount account
            where account.user.id = :userId and account.active = true
            """)
    BigDecimal sumActiveOpeningBalancesByUserId(@Param("userId") Long userId);
}
