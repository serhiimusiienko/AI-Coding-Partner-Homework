package com.example.banking.dto;

import com.example.banking.validation.CurrencyCode;
import com.example.banking.validation.MaxTwoDecimalPlaces;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class CreateTransactionRequest {
    @NotBlank
    @Pattern(regexp = "ACC-[A-Za-z0-9]{5}")
    private String fromAccount;

    @NotBlank
    @Pattern(regexp = "ACC-[A-Za-z0-9]{5}")
    private String toAccount;

    @NotNull
    @MaxTwoDecimalPlaces
    private BigDecimal amount;

    @NotBlank
    @CurrencyCode
    private String currency;

    @NotBlank
    @Pattern(regexp = "^(deposit|withdrawal|transfer)$")
    private String type;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
