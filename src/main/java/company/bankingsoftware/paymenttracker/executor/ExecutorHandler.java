package company.bankingsoftware.paymenttracker.executor;

import company.bankingsoftware.paymenttracker.model.PaymentEvent;
import company.bankingsoftware.paymenttracker.model.TransactionLedger;
import company.bankingsoftware.paymenttracker.reader.CommandLinePaymentReader;
import company.bankingsoftware.paymenttracker.reader.FilePaymentReader;
import company.bankingsoftware.paymenttracker.reader.RegexPaymentParser;
import company.bankingsoftware.paymenttracker.printer.ConsoleTransactionLedgerOutput;
import company.bankingsoftware.paymenttracker.service.InputOutputTransactionLedgerService;
import company.bankingsoftware.paymenttracker.service.QueueSchedulerTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class:
 * - Initiates payment reader executor service and scheduled executor service.
 * - Initiates 2 blocking queues - input queue for payment events and output queue with transaction ledger.
 * - Cleans the futures (by cancel) and executor services (by shutdown command) when the <i>quit</i> (or exit the program) command is triggered
 *
 * Producers of <i>ADD payment event type</i> events into input queue are file input reader and command line input reader.
 * Producer of <i>OUTPUT payment type event</i> is queue scheduler task and it generates events in 60 second intervals.
 * Producer of <i>SHUTDOWN payment type event</i> is this class (also forces transaction ledger service if graceful ending failed).
 * Consumer of input queue is transaction ledger service.
 *
 * Producer into output queue is transaction ledger service (by consuming input queue).
 * Consumer of output queue is transaction ledger output.

 * File input reader, command line input reader, transaction ledger service and transaction ledger output
 * shares the payment reader executor service thread pool.
 * Queue scheduler task is the only one task in scheduled executor service single thread pool.
 */
public class ExecutorHandler {

    private static final Logger LOGGER = Logger.getLogger(ExecutorHandler.class.getName());

    private static final int PAYMENT_READER_POOL_SIZE = 3;
    private static final int INPUT_PAYMENT_EVENTS_QUEUE_SIZE = 50;
    private static final int OUTPUT_TRANSACTION_LEDGER_QUEUE_SIZE = 20;
    private static final int TRANSACTION_LEDGER_TIMEOUT = 10;
    private static final int SCHEDULER_DELAY = 60;

    private final Path path;
    private final ExecutorService paymentReaderExecutorService;
    private final BlockingQueue<PaymentEvent> inputPaymentEventsQueue;
    private final BlockingQueue<TransactionLedger> outputTransactionLedgerQueue;
    private final ScheduledExecutorService schedulerExecutorService;

    private final InputOutputTransactionLedgerService inputOutputTransactionLedgerService;
    private final CommandLinePaymentReader commandLinePaymentReader;
    private final ConsoleTransactionLedgerOutput consoleTransactionLedgerOutput;

    private final QueueSchedulerTask queueSchedulerTask;

    private FilePaymentReader filePaymentReader;

    public ExecutorHandler(String filePath, Level logLevel) {
        this.path = filePath != null && !filePath.isEmpty() ? Paths.get(filePath) : null;
        LOGGER.setLevel(logLevel);

        paymentReaderExecutorService = Executors.newFixedThreadPool(PAYMENT_READER_POOL_SIZE);
        inputPaymentEventsQueue = new ArrayBlockingQueue<>(INPUT_PAYMENT_EVENTS_QUEUE_SIZE);
        outputTransactionLedgerQueue = new ArrayBlockingQueue<>(OUTPUT_TRANSACTION_LEDGER_QUEUE_SIZE);
        schedulerExecutorService = Executors.newSingleThreadScheduledExecutor();

        if (path != null) {
            this.filePaymentReader = new FilePaymentReader(path, inputPaymentEventsQueue, new RegexPaymentParser(), logLevel);
        }

        this.inputOutputTransactionLedgerService = new InputOutputTransactionLedgerService(inputPaymentEventsQueue, outputTransactionLedgerQueue, logLevel);
        this.commandLinePaymentReader = new CommandLinePaymentReader(System.in, inputPaymentEventsQueue, new RegexPaymentParser(), logLevel);
        this.consoleTransactionLedgerOutput = new ConsoleTransactionLedgerOutput(System.out, outputTransactionLedgerQueue, logLevel);
        this.queueSchedulerTask = new QueueSchedulerTask(inputPaymentEventsQueue, logLevel);
    }

    public void handleFutures() {
        Future<?> inputOutputTransactionLedgerService = paymentReaderExecutorService.submit(this.inputOutputTransactionLedgerService);

        // to get inputOutputTransactionLedgerService
        // to shutdown paymentReaderExecutorService

        if (this.filePaymentReader != null) {
            Future<?> filePaymentReader = paymentReaderExecutorService.submit(this.filePaymentReader);
            try {
                filePaymentReader.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.SEVERE, "File payment reader failed.");
                filePaymentReader.cancel(true);
            }
        }

        Future<?> commandLinePaymentReader = paymentReaderExecutorService.submit(this.commandLinePaymentReader);

        // to get inputOutputTransactionLedgerService, commandLinePaymentReader
        // to shutdown paymentReaderExecutorService

        Future<?> consoleTransactionLedgerOutput = paymentReaderExecutorService.submit(this.consoleTransactionLedgerOutput);

        // to get inputOutputTransactionLedgerService, commandLinePaymentReader
        // to cancel consoleTransactionLedgerOutput
        // to shutdown paymentReaderExecutorService

        Future<?> queueSchedulerTask = schedulerExecutorService.scheduleWithFixedDelay(
                this.queueSchedulerTask,
                SCHEDULER_DELAY,
                SCHEDULER_DELAY,
                TimeUnit.SECONDS
        );

        // to get inputOutputTransactionLedgerService, commandLinePaymentReader
        // to cancel consoleTransactionLedgerOutput, queueSchedulerTask
        // to shutdown paymentReaderExecutorService, schedulerExecutorService

        try {
            commandLinePaymentReader.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Command line payment reader failed.");
            commandLinePaymentReader.cancel(true);
        }

        // to get inputOutputTransactionLedgerService
        // to cancel consoleTransactionLedgerOutput, queueSchedulerTask
        // to shutdown paymentReaderExecutorService, schedulerExecutorService

        try {
            LOGGER.log(Level.INFO, "Sending payment event - SHUTDOWN.");
            inputPaymentEventsQueue.put(PaymentEvent.builder().withPaymentEventType(PaymentEvent.PaymentEventType.SHUTDOWN).build());
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "SHUTDOWN payment event failed.");
        }

        try {
            LOGGER.log(Level.INFO, "Force SHUTDOWN.");
            inputOutputTransactionLedgerService.get(TRANSACTION_LEDGER_TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            inputOutputTransactionLedgerService.cancel(true);
        }

        // to cancel consoleTransactionLedgerOutput, queueSchedulerTask
        // to shutdown paymentReaderExecutorService, schedulerExecutorService

        queueSchedulerTask.cancel(true);
        consoleTransactionLedgerOutput.cancel(true);
        schedulerExecutorService.shutdown();
        paymentReaderExecutorService.shutdown();
    }
}
