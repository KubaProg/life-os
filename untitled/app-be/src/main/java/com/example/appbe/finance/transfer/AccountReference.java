package com.example.appbe.finance.transfer;

import com.example.appbe.finance.account.AccountKind;

/** Family is response metadata for routing; accounts.id is globally unique. */
public record AccountReference(AccountKind kind, Long id) {}
