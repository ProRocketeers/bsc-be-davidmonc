package company.bankingsoftware.paymenttracker.service;

import company.bankingsoftware.paymenttracker.model.Payment;
import company.bankingsoftware.paymenttracker.model.PaymentEvent;
import company.bankingsoftware.paymenttracker.model.TransactionLedger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class InputOutputTransactionLedgerServiceTest {

    private InputOutputTransactionLedgerService transactionLedgerService;

    @Mock
    BlockingQueue<PaymentEvent> inputPaymentEventsQueueMock;
    @Mock
    BlockingQueue<TransactionLedger> outputTransactionLedgerQueueMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void queueTransactionLedger_withItemToPutIntoQueue_shouldPutItemIntoQueue() throws InterruptedException {
        transactionLedgerService = new InputOutputTransactionLedgerService(inputPaymentEventsQueueMock, outputTransactionLedgerQueueMock, Level.OFF);
        transactionLedgerService.queueTransactionLedger();

        Mockito.verify(outputTransactionLedgerQueueMock, Mockito.times(1)).put(Mockito.any());
    }

    @Test
    public void takeInputPaymentEvent_withInputPaymentEventInQueue_shouldReturnPaymentEvent() throws InterruptedException {
        final PaymentEvent paymentEvent = PaymentEvent.builder().build();
        Mockito.when(inputPaymentEventsQueueMock.take()).thenReturn(paymentEvent);

        transactionLedgerService = new InputOutputTransactionLedgerService(inputPaymentEventsQueueMock, outputTransactionLedgerQueueMock, Level.OFF);
        assertThat(transactionLedgerService.takeInputPaymentEvent(), is(paymentEvent));
    }

    @Test
    public void takeInputPaymentEvent_withNotAbleToTake_shouldReturnNull() throws InterruptedException {
        Mockito.when(inputPaymentEventsQueueMock.take()).thenThrow(InterruptedException.class);

        transactionLedgerService = new InputOutputTransactionLedgerService(inputPaymentEventsQueueMock, outputTransactionLedgerQueueMock, Level.OFF);
        assertThat(transactionLedgerService.takeInputPaymentEvent(), is(nullValue()));
    }
}
