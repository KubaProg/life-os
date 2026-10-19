package com.example.appbe.finance.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {
    @Query("select f from FinancialAccount f where f.account.active = true order by f.account.name")
    List<FinancialAccount> findByActiveTrueOrderByNameAsc();

    @Query("select f from FinancialAccount f where f.id = :id and f.account.active = true")
    Optional<FinancialAccount> findByIdAndActiveTrue(@Param("id") Long id);

    @Query("select f from FinancialAccount f where f.id = :id and f.account.user.id = :userId and f.account.active = true")
    Optional<FinancialAccount> findByIdAndUserIdAndActiveTrue(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select f from FinancialAccount f where f.account.user.id = :userId and f.account.active = true order by f.account.name")
    List<FinancialAccount> findByUserIdAndActiveTrueOrderByNameAsc(@Param("userId") Long userId);

    @Query("select f from FinancialAccount f where f.account.user.id = :userId and f.type = :type")
    List<FinancialAccount> findByUserIdAndType(@Param("userId") Long userId, @Param("type") FinancialAccountType type);

    @Query("select coalesce(sum(f.openingBalance), 0) from FinancialAccount f where f.account.user.id = :userId and f.account.active = true")
    BigDecimal sumActiveOpeningBalancesByUserId(@Param("userId") Long userId);
}
