package com.example.appbe.finance.bond;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondRepository extends JpaRepository<Bond, Long> {
    List<Bond> findByAccountIdOrderByIdDesc(Long accountId);
    List<Bond> findByUserIdOrderByIdAsc(Long userId);
    Optional<Bond> findByIdAndAccountId(Long id, Long accountId);
}
