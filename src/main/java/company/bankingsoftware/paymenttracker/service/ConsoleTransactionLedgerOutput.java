package company.bankingsoftware.paymenttracker.service;

import company.bankingsoftware.paymenttracker.model.TransactionLedger;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple console output implementation.
 */
public class ConsoleTransactionLedgerOutput implements TransactionLedgerOutput {

    private static final Logger LOGGER = Logger.getLogger(ConsoleTransactionLedgerOutput.class.getName());

    private final PrintStream outputStream;
    private final BlockingQueue<TransactionLedger> outputTransactionLedgerQueue;
    private boolean running = true;

    public ConsoleTransactionLedgerOutput(
            PrintStream outputStream,
            BlockingQueue<TransactionLedger> outputTransactionLedgerQueue,
            Level logLevel) {
        this.outputStream = outputStream;
        this.outputTransactionLedgerQueue = outputTransactionLedgerQueue;
        LOGGER.setLevel(logLevel);
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
            outputStream.println(transactionLedger.getPaymentBalances().toString());
            outputStream.flush();
        } catch (InterruptedException ie) {
            running = false;
            LOGGER.log(Level.INFO, "Interruption - not able to take from transaction ledger output queue.");
        }
    }

}
