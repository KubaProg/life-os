package com.example.appbe.finance.brokerage;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrokerageAccountRepository extends JpaRepository<BrokerageAccount, Long> {
    @Query("select a from BrokerageAccount a where a.account.user.id = :userId and a.account.active = true order by a.id")
    List<BrokerageAccount> findOwned(@Param("userId") Long userId);
    @Query("select a from BrokerageAccount a where a.id = :id and a.account.user.id = :userId and a.account.active = true")
    Optional<BrokerageAccount> findOwned(@Param("id") Long id, @Param("userId") Long userId);
}
