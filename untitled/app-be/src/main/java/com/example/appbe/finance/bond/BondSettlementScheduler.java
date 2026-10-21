package com.example.appbe.finance.bond;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Durable due dates live in SQL. Restart catches up; reads also settle before exposing balances. */
@Component
public class BondSettlementScheduler {
    private final BondService bonds;
    public BondSettlementScheduler(BondService bonds) { this.bonds = bonds; }
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() { bonds.settleDue(); }
    @Scheduled(cron = "0 5 * * * *", zone = "Europe/Warsaw")
    public void settle() { bonds.settleDue(); }
}
