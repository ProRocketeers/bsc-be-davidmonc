package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.PaymentEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Implementation of PaymentReader, reads the given file as an input.
 */
public class FilePaymentReader implements PaymentReader {

    private static final Logger LOGGER = Logger.getLogger(FilePaymentReader.class.getName());

    private final Path path;
    private final BlockingQueue<PaymentEvent> inputPaymentEventsQueue;
    private final PaymentParser paymentParser;

    public FilePaymentReader(
            Path path,
            BlockingQueue<PaymentEvent> inputPaymentEventsQueue,
            PaymentParser paymentParser,
            Level logLevel) {
        this.path = path;
        this.inputPaymentEventsQueue = inputPaymentEventsQueue;
        this.paymentParser = paymentParser;
        LOGGER.setLevel(logLevel);
    }

    @Override
    public void run() {
        readFileInput();
    }

    public void readFileInput() {
        try (Stream<String> stream = Files.lines(path)) {

            stream
                    .filter(paymentLine -> !paymentLine.isEmpty())
                    .forEach(paymentLine -> queue(parsePaymentEvent(paymentLine)));

        } catch (IOException ioe ) {
            LOGGER.log(Level.SEVERE, "Failed to read input file.");
        }
    }

    PaymentEvent parsePaymentEvent(String paymentLine) {
        try {
            return PaymentEvent.builder()
                    .withPaymentEventType(PaymentEvent.PaymentEventType.ADD)
                    .withPayment(paymentParser.toPayment(paymentLine))
                    .build();
        } catch (ParseException pe) {
            LOGGER.log(Level.SEVERE, "Payment parsing failed.");
        }

        return null;
    }

    void queue(PaymentEvent paymentEvent) {
        try {
            LOGGER.log(Level.INFO, "Putting payment event into queue - {0}", paymentEvent);
            if (paymentEvent != null) {
                inputPaymentEventsQueue.put(paymentEvent);
            }
        } catch (InterruptedException ie) {
            LOGGER.log(Level.SEVERE, "Putting payment event into queue failed.");
        }
    }
}
