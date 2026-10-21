package com.example.appbe.finance.bond;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondIssueRepository extends JpaRepository<BondIssue, Long> {
    List<BondIssue> findAllByOrderBySaleFromDesc();
}
