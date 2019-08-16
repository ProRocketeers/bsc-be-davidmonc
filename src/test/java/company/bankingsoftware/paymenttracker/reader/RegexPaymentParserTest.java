package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class RegexPaymentParserTest {

    private RegexPaymentParser regexPaymentParser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        regexPaymentParser = new RegexPaymentParser();
    }

    @Test
    public void toPayment_withValidPaymentLine_shouldReturnPayment() throws ParseException {
        final Payment payment = regexPaymentParser.toPayment("USD 1000");
        assertThat(payment.getCurrency(), is("USD"));
        assertThat(payment.getAmount(), is(BigDecimal.valueOf(1000)));
    }

    @Test
    public void toPayment_withNegativeValidPaymentLine_shouldReturnPayment() throws ParseException {
        final Payment payment = regexPaymentParser.toPayment("USD -1000");
        assertThat(payment.getCurrency(), is("USD"));
        assertThat(payment.getAmount(), is(BigDecimal.valueOf(-1000)));
    }

    @Test
    public void toPayment_withNegativeAndDecimalPointValidPaymentLine_shouldReturnPayment() throws ParseException {
        final Payment payment = regexPaymentParser.toPayment("USD -1000.0");
        assertThat(payment.getCurrency(), is("USD"));
        assertThat(payment.getAmount(), is(BigDecimal.valueOf(-1000.0)));
    }

    @Test
    public void toPayment_withSmallAmountPaymentLine_shouldReturnPayment() throws ParseException {
        final Payment payment = regexPaymentParser.toPayment("USD 1");
        assertThat(payment.getCurrency(), is("USD"));
        assertThat(payment.getAmount(), is(BigDecimal.valueOf(1)));
    }

    // negative cases
    @Test(expected = ParseException.class)
    public void toPayment_withValidPaymentWithSuffix_shouldThrowException() throws ParseException {
        regexPaymentParser.toPayment("USD 1000 ABCD1234 aa -100. .");
    }

    @Test(expected = ParseException.class)
    public void toPayment_withValidPaymentWithPrefix_shouldThrowException() throws ParseException {
        regexPaymentParser.toPayment(" USD 1000");
    }

    @Test(expected = ParseException.class)
    public void toPayment_withInvalidCurrency_shouldThrowException() throws ParseException {
        regexPaymentParser.toPayment("US 1000");
    }

    @Test(expected = ParseException.class)
    public void toPayment_withAmountAndDecimalPointOnlyInvalidPaymentLine_shouldThrowException() throws ParseException {
        regexPaymentParser.toPayment("USD 1000.");
    }

    @Test(expected = ParseException.class)
    public void toPayment_withDecimalPointOnlyInvalidPaymentLine_shouldThrowException() throws ParseException {
        regexPaymentParser.toPayment("USD .");
    }
}
