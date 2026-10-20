package com.example.appbe.finance.investment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepositAccountRepository extends JpaRepository<DepositAccount, Long> {
    @Query("select i from DepositAccount i where i.account.user.id = :userId order by i.id")
    List<DepositAccount> findByUserIdOrderByIdAsc(@Param("userId") Long userId);

    @Query("select i from DepositAccount i where i.id = :id and i.account.user.id = :userId")
    Optional<DepositAccount> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
