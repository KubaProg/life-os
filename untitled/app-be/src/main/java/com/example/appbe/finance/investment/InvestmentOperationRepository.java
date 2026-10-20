package com.example.appbe.finance.investment;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentOperationRepository extends JpaRepository<InvestmentOperation, Long> {
    List<InvestmentOperation> findByInvestmentIdOrderByOperationDateAscIdAsc(Long investmentId);
    @Query("""
            select o from InvestmentOperation o where o.transfer.user.id = :userId
              and o.type = :type and o.operationDate between :from and :to
            """)
    List<InvestmentOperation> findByInvestmentUserIdAndTypeAndOperationDateBetween(
            @Param("userId") Long userId, @Param("type") String type,
            @Param("from") LocalDate from, @Param("to") LocalDate to);
}
