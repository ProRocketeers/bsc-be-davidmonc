package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;

import java.text.ParseException;

/**
 * Interface for parsing an input into payment.
 */
public interface PaymentParser {

    /**
     * Maps payment line into Payment object.
     * @param paymentLine
     * @return the Payment object
     * @throws ParseException
     */
    Payment toPayment(String paymentLine) throws ParseException;
}
