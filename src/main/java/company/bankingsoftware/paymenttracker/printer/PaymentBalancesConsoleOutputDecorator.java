package company.bankingsoftware.paymenttracker.printer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of decorator, exchanges to USD.
 */
public class PaymentBalancesConsoleOutputDecorator implements PaymentBalancesOutputDecorator {

    private static final String FROM_EXCHANGE = "USD";
    private static Map<String, BigDecimal> exchangeRates;

    public PaymentBalancesConsoleOutputDecorator() {
        // read that from a real service
        this.exchangeRates = Collections.unmodifiableMap(new HashMap<>() {{
            put("CZK", BigDecimal.valueOf(0.04));
            put("GBP", BigDecimal.valueOf(1.21));
            put("EUR", BigDecimal.valueOf(1.11));
            put("RMB", BigDecimal.valueOf(0.14));
            put("HKD", BigDecimal.valueOf(0.13));
        }});
    }

    @Override
    public String decorate(Map<String, BigDecimal> paymentBalances) {
        StringBuilder sb = new StringBuilder();
        for (String currency : paymentBalances.keySet()) {
            sb.append(currency).append(" ").append(paymentBalances.get(currency));
            if (currency != FROM_EXCHANGE && exchangeRates.get(currency) != null) {
                BigDecimal multiplyResult = exchangeRates.get(currency).multiply(paymentBalances.get(currency));
                sb.append(" (USD ").append(multiplyResult.setScale(2, RoundingMode.HALF_UP)).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
