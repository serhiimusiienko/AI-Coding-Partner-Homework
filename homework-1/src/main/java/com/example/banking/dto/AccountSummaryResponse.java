package com.example.banking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountSummaryResponse {
    private String accountId;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private long transactionCount;
    private Instant mostRecentTransactionDate;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public Instant getMostRecentTransactionDate() {
        return mostRecentTransactionDate;
    }

    public void setMostRecentTransactionDate(Instant mostRecentTransactionDate) {
        this.mostRecentTransactionDate = mostRecentTransactionDate;
    }
}
