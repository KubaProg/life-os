package com.example.appbe.finance.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}
