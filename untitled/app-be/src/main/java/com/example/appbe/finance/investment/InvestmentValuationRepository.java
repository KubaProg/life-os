package com.example.appbe.finance.investment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InvestmentValuationRepository extends JpaRepository<InvestmentValuation, Long> {
    List<InvestmentValuation> findByInvestmentIdOrderByValuationDateAsc(Long investmentId);
}
