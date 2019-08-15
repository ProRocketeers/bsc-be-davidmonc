package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.PaymentEvent;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of PaymentReader, reads the command line input.
 */
public class CommandLinePaymentReader implements PaymentReader {

    private static final Logger LOGGER = Logger.getLogger(CommandLinePaymentReader.class.getName());

    private static final String QUIT_COMMAND = "quit";

    private final InputStream inputStream;
    private final BlockingQueue<PaymentEvent> inputPaymentEventsQueue;
    private final PaymentParser paymentParser;

    public CommandLinePaymentReader(
            InputStream inputStream,
            BlockingQueue<PaymentEvent> inputPaymentEventsQueue,
            PaymentParser paymentParser,
            Level logLevel) {
        this.inputStream = inputStream;
        this.inputPaymentEventsQueue = inputPaymentEventsQueue;
        this.paymentParser = paymentParser;
        LOGGER.setLevel(logLevel);
    }

    @Override
    public void run() {
        readCommandLineInput();
    }

    public void readCommandLineInput() {
        Scanner scanner = new Scanner(inputStream);

        for (String commandLineInput; scanner.hasNext() && !QUIT_COMMAND.equals(commandLineInput = scanner.nextLine());) {
            queue(parsePaymentEvent(commandLineInput));
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
