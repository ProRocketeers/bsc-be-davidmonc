package company.bankingsoftware.paymenttracker.printer;

import company.bankingsoftware.paymenttracker.model.TransactionLedger;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Simple console output implementation.
 */
public class ConsoleTransactionLedgerOutput implements TransactionLedgerOutput {

    private static final Logger LOGGER = Logger.getLogger(ConsoleTransactionLedgerOutput.class.getName());

    private final PrintStream outputStream;
    private final BlockingQueue<TransactionLedger> outputTransactionLedgerQueue;
    private final PaymentBalancesOutputDecorator transactionLedgerOutputDecorator;
    private boolean running = true;

    public ConsoleTransactionLedgerOutput(
            PrintStream outputStream,
            BlockingQueue<TransactionLedger> outputTransactionLedgerQueue,
            Level logLevel) {
        this.outputStream = outputStream;
        this.outputTransactionLedgerQueue = outputTransactionLedgerQueue;
        LOGGER.setLevel(logLevel);
        this.transactionLedgerOutputDecorator = new PaymentBalancesConsoleOutputDecorator();
    }

    @Override
    public void run() {
        while (running) {
            print();
        }
    }

    void print() {
        try {
            TransactionLedger transactionLedger = outputTransactionLedgerQueue.take();

            LOGGER.log(Level.INFO, "To decorate: {0}", transactionLedger.getPaymentBalances().toString());
            if (transactionLedger.getPaymentBalances().size() > 0) {
                String exchangeRateOutput = transactionLedgerOutputDecorator.decorate(
                        transactionLedger.getPaymentBalances().entrySet().stream()
                                // equals does not work here
                                .filter(item -> item.getValue().compareTo(BigDecimal.ZERO) != 0)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                );
                outputStream.print(exchangeRateOutput);
                outputStream.flush();
            }
        } catch (InterruptedException ie) {
            running = false;
            LOGGER.log(Level.INFO, "Interruption - not able to take from transaction ledger output queue.");
        }
    }

}
