package company.bankingsoftware.paymenttracker.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TransactionLedgerTest {

    private final static String USD = "USD";
    private final static String GBP = "GBP";
    private final static String CZK = "CZK";

    private TransactionLedger transactionLedger;

    @Test
    public void addPayment_withInitializationOnly_shouldReturnPaymentBalances() {
        assertThat(new TransactionLedger().getPaymentBalances(), is(notNullValue()));
    }

    @Test
    public void addPayment_withSameCurrencyAdd_shouldReturnSum() {
        transactionLedger = new TransactionLedger();
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(USD)
                        .withAmount(BigDecimal.TEN)
                        .build()
        );
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(USD)
                        .withAmount(BigDecimal.ONE)
                        .build()
        );

        assertThat(transactionLedger.getPaymentBalances().get(USD), is(BigDecimal.valueOf(11)));
    }

    @Test
    public void addPayment_withSameCurrencyAddPositiveAndNegative_shouldReturnZero() {
        transactionLedger = new TransactionLedger();
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(USD)
                        .withAmount(BigDecimal.TEN)
                        .build()
        );
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(USD)
                        .withAmount(BigDecimal.valueOf(-10))
                        .build()
        );

        assertThat(transactionLedger.getPaymentBalances().get(USD), is(BigDecimal.ZERO));
    }

    @Test
    public void addPayment_withDifferentCurrencyAdd_shouldReturnSum() {
        transactionLedger = new TransactionLedger();
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(USD)
                        .withAmount(BigDecimal.TEN)
                        .build()
        );
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(CZK)
                        .withAmount(BigDecimal.ONE)
                        .build()
        );
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(GBP)
                        .withAmount(BigDecimal.ZERO)
                        .build()
        );

        assertThat(transactionLedger.getPaymentBalances().get(USD), is(BigDecimal.TEN));
        assertThat(transactionLedger.getPaymentBalances().get(CZK), is(BigDecimal.ONE));
        assertThat(transactionLedger.getPaymentBalances().get(GBP), is(BigDecimal.ZERO));
    }

    @Test
    public void addPayment_withSameCurrencyDifferentPointNumberPosition_shouldReturnZero() {
        transactionLedger = new TransactionLedger();
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(CZK)
                        .withAmount(BigDecimal.TEN)
                        .build()
        );
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(CZK)
                        .withAmount(BigDecimal.valueOf(0.1))
                        .build()
        );
        transactionLedger.addPayment(
                Payment.builder()
                        .withCurrency(CZK)
                        .withAmount(BigDecimal.valueOf(0.10))
                        .build()
        );

        assertThat(transactionLedger.getPaymentBalances().get(CZK), is(BigDecimal.valueOf(10.20)));
    }
}
