package company.bankingsoftware.paymenttracker.service;

import company.bankingsoftware.paymenttracker.model.Payment;
import company.bankingsoftware.paymenttracker.model.PaymentEvent;
import company.bankingsoftware.paymenttracker.model.TransactionLedger;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputOutputTransactionLedgerService implements TransactionLedgerService {

    private static final Logger LOGGER = Logger.getLogger(InputOutputTransactionLedgerService.class.getName());

    private final BlockingQueue<PaymentEvent> inputPaymentEventsQueue;
    private final BlockingQueue<TransactionLedger> outputTransactionLedgerQueue;
    private final TransactionLedger transactionLedger;
    private boolean running = true;

    public InputOutputTransactionLedgerService(
            BlockingQueue<PaymentEvent> inputPaymentEventsQueue,
            BlockingQueue<TransactionLedger> outputTransactionLedgerQueue,
            Level logLevel) {
        this.inputPaymentEventsQueue = inputPaymentEventsQueue;
        this.outputTransactionLedgerQueue = outputTransactionLedgerQueue;
        transactionLedger = new TransactionLedger();
        LOGGER.setLevel(logLevel);
    }

    @Override
    public void run() {
        while (running) {
                PaymentEvent paymentEvent = takeInputPaymentEvent();

                if (paymentEvent == null) {
                    continue;
                }
                switch (paymentEvent.getPaymentEventType()) {
                    case ADD:
                        addPaymentIntoTransactionLedger(paymentEvent.getPayment());
                        break;
                    case OUTPUT:
                        queueTransactionLedger();
                        break;
                    case SHUTDOWN:
                        running = false;
                        break;
                }
        }
    }

    void queueTransactionLedger() {
        try {
            LOGGER.log(Level.INFO, "Output transaction ledger.");
            outputTransactionLedgerQueue.put(transactionLedger);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to put into output queue.");
        }
    }

    PaymentEvent takeInputPaymentEvent() {
        try {
            LOGGER.log(Level.INFO, "Waiting for input in payment event queue");
            PaymentEvent paymentEvent = inputPaymentEventsQueue.take();
            LOGGER.log(Level.INFO, "Taking {0}", paymentEvent.getPaymentEventType());
            return paymentEvent;
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to take from input queue.");
            return null;
        }
    }

    private void addPaymentIntoTransactionLedger(Payment payment) {
        LOGGER.log(Level.INFO, "Adding payment into transaction ledger.");
        transactionLedger.addPayment(payment);
    }
}
