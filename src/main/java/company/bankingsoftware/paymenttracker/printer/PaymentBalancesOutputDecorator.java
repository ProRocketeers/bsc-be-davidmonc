package company.bankingsoftware.paymenttracker.printer;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Decorator interface.
 */
public interface PaymentBalancesOutputDecorator {
    String decorate(Map<String, BigDecimal> paymentBalances);
}
