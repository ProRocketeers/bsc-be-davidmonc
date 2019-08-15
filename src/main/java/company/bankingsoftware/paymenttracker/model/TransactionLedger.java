package company.bankingsoftware.paymenttracker.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Transaction ledger - balance of net payments,
 */
public class TransactionLedger {

    private final Map<String, BigDecimal> paymentBalances;

    public TransactionLedger() {
        paymentBalances = new HashMap<>();
    }

    public TransactionLedger addPayment(Payment payment) {
        paymentBalances.put(
                payment.getCurrency(),
                paymentBalances.getOrDefault(payment.getCurrency(), BigDecimal.ZERO).add(payment.getAmount()));
        return this;
    }

    public Map<String, BigDecimal> getPaymentBalances() {
        return paymentBalances;
    }
}
