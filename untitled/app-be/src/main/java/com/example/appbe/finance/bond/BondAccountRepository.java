package com.example.appbe.finance.bond;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BondAccountRepository extends JpaRepository<BondAccount, Long> {
    @Query("select a from BondAccount a where a.account.user.id = :userId and a.account.active = true order by a.id")
    List<BondAccount> findOwned(@Param("userId") Long userId);
    @Query("select a from BondAccount a where a.id = :id and a.account.user.id = :userId and a.account.active = true")
    Optional<BondAccount> findOwned(@Param("id") Long id, @Param("userId") Long userId);
}
