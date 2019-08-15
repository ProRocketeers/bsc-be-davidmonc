package company.bankingsoftware.paymenttracker.executor;

import company.bankingsoftware.paymenttracker.model.PaymentEvent;
import company.bankingsoftware.paymenttracker.model.TransactionLedger;
import company.bankingsoftware.paymenttracker.reader.CommandLinePaymentReader;
import company.bankingsoftware.paymenttracker.reader.FilePaymentReader;
import company.bankingsoftware.paymenttracker.reader.RegexPaymentParser;
import company.bankingsoftware.paymenttracker.service.ConsoleTransactionLedgerOutput;
import company.bankingsoftware.paymenttracker.service.InMemoryTransactionLedgerService;
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

public class ExecutorHandler {

    private static final Logger LOGGER = Logger.getLogger(ExecutorHandler.class.getName());

    private final Path path;
    private final ExecutorService paymentReaderExecutorService;
    private final BlockingQueue<PaymentEvent> inputPaymentEventsQueue;
    private final BlockingQueue<TransactionLedger> outputTransactionLedgerQueue;
    private final ScheduledExecutorService schedulerExecutorService;

    private final InMemoryTransactionLedgerService inMemoryTransactionLedgerService;
    private final CommandLinePaymentReader commandLinePaymentReader;
    private final ConsoleTransactionLedgerOutput consoleTransactionLedgerOutput;

    private final QueueSchedulerTask queueSchedulerTask;

    private FilePaymentReader filePaymentReader;

    public ExecutorHandler(String filePath, Level logLevel) {
        this.path = filePath != null && !filePath.isEmpty() ? Paths.get(filePath) : null;
        LOGGER.setLevel(logLevel);

        paymentReaderExecutorService = Executors.newFixedThreadPool(3);
        inputPaymentEventsQueue = new ArrayBlockingQueue<>(50);
        outputTransactionLedgerQueue = new ArrayBlockingQueue<>(20);
        schedulerExecutorService = Executors.newSingleThreadScheduledExecutor();

        if (path != null) {
            this.filePaymentReader = new FilePaymentReader(path, inputPaymentEventsQueue, new RegexPaymentParser(), logLevel);
        }

        this.inMemoryTransactionLedgerService = new InMemoryTransactionLedgerService(inputPaymentEventsQueue, outputTransactionLedgerQueue, logLevel);
        this.commandLinePaymentReader = new CommandLinePaymentReader(System.in, inputPaymentEventsQueue, new RegexPaymentParser(), logLevel);
        this.consoleTransactionLedgerOutput = new ConsoleTransactionLedgerOutput(System.out, outputTransactionLedgerQueue, logLevel);
        this.queueSchedulerTask = new QueueSchedulerTask(inputPaymentEventsQueue, logLevel);
    }

    public void handleFutures() {
        Future<?> inMemoryTransactionLedgerService = paymentReaderExecutorService.submit(this.inMemoryTransactionLedgerService);

        // to get inMemoryTransactionLedgerService
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

        // to get inMemoryTransactionLedgerService, commandLinePaymentReader
        // to shutdown paymentReaderExecutorService

        Future<?> consoleTransactionLedgerOutput = paymentReaderExecutorService.submit(this.consoleTransactionLedgerOutput);

        // to get inMemoryTransactionLedgerService, commandLinePaymentReader
        // to cancel consoleTransactionLedgerOutput
        // to shutdown paymentReaderExecutorService

        Future<?> queueSchedulerTask = schedulerExecutorService.scheduleWithFixedDelay(
                this.queueSchedulerTask,
                15,
                15,
                TimeUnit.SECONDS
        );

        // to get inMemoryTransactionLedgerService, commandLinePaymentReader
        // to cancel consoleTransactionLedgerOutput, queueSchedulerTask
        // to shutdown paymentReaderExecutorService, schedulerExecutorService

        try {
            commandLinePaymentReader.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Command line payment reader failed.");
            commandLinePaymentReader.cancel(true);
        }

        // to get inMemoryTransactionLedgerService
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
            inMemoryTransactionLedgerService.get(10, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            inMemoryTransactionLedgerService.cancel(true);
        }

        // to cancel consoleTransactionLedgerOutput, queueSchedulerTask
        // to shutdown paymentReaderExecutorService, schedulerExecutorService

        queueSchedulerTask.cancel(true);
        consoleTransactionLedgerOutput.cancel(true);
        schedulerExecutorService.shutdown();
        paymentReaderExecutorService.shutdown();
    }
}
