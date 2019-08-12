package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;

/**
 * Interface for parsing an input into payment.
 */
public interface PaymentParser {

    Payment toPayment(String paymentLine);
}
