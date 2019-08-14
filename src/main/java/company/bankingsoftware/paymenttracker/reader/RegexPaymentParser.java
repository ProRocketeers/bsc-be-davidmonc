package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Regex implementation of payment parser.
 */
public class RegexPaymentParser implements PaymentParser {

    private final static Pattern PAYMENT_LINE_PATTERN = Pattern.compile("[A-Z]{3}\\s+[-]?\\d+(\\.\\d{1,2})?");

    @Override
    public Payment toPayment(String paymentLine) throws ParseException {
        if (!validatePaymentLine(paymentLine)) {
            throw new ParseException(String.format("Invalid payment line: %s", paymentLine), 0);
        }

        StringTokenizer stringTokenizer = new StringTokenizer(paymentLine, " ");
        return Payment.builder()
                .withCurrency(stringTokenizer.nextToken())
                .withAmount(new BigDecimal(stringTokenizer.nextToken()))
                .build();
    }

    private boolean validatePaymentLine(String paymentLine) {
        boolean valid = true;
        if (paymentLine == null || !PAYMENT_LINE_PATTERN.matcher(paymentLine).matches()) {
            valid = false;
        }

        return valid;
    }
}
