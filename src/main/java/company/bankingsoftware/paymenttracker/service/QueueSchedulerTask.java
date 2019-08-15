package company.bankingsoftware.paymenttracker.service;

import company.bankingsoftware.paymenttracker.model.PaymentEvent;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Queue scheduler, puts OUTPUT event type into input queue.
 */
public class QueueSchedulerTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(QueueSchedulerTask.class.getName());

    private final BlockingQueue<PaymentEvent> paymentEventsQueue;

    public QueueSchedulerTask(BlockingQueue<PaymentEvent> paymentEventsQueue, Level logLevel) {
        this.paymentEventsQueue = paymentEventsQueue;
        LOGGER.setLevel(logLevel);
    }

    @Override
    public void run() {
        try {
            LOGGER.log(Level.INFO, "Putting scheduled OUTPUT into input payment event queue.");
            paymentEventsQueue.put(
                PaymentEvent.builder()
                        .withPaymentEventType(PaymentEvent.PaymentEventType.OUTPUT)
                        .build()
            );
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Queue scheduler failed to put OUTPUT task.");
        }
    }
}
