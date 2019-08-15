package company.bankingsoftware.paymenttracker.model;

import java.math.BigDecimal;

/**
 * Currency and amount couple.
 */
public class Payment {

    private final String currency;
    private final BigDecimal amount;

    private Payment(Builder b) {
        this.currency = b.currency;
        this.amount = b.amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String currency;
        private BigDecimal amount;

        private Builder() {}

        public Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
