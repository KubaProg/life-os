package com.example.appbe.finance.bond;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondOperationRepository extends JpaRepository<BondOperation, Long> {
    Optional<BondOperation> findByRequestId(String requestId);
    boolean existsByEventKey(String eventKey);
    List<BondOperation> findByBondAccountIdOrderBySettlementDateAscIdAsc(Long accountId);
    List<BondOperation> findByBondUserIdOrderBySettlementDateAscIdAsc(Long userId);
    List<BondOperation> findByBondIdOrderBySettlementDateAscIdAsc(Long bondId);
}
