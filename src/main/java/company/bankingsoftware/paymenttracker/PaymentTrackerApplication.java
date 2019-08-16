package company.bankingsoftware.paymenttracker;

import company.bankingsoftware.paymenttracker.executor.ExecutorHandler;

import java.util.logging.Level;

public class PaymentTrackerApplication {

    public static void main(String[] args) {
        // change 2nd parameter to Level.INFO to see INFO messages about processing
        ExecutorHandler executorHandler = new ExecutorHandler(args.length > 0 ? args[0] : null, Level.SEVERE);
        executorHandler.handleFutures();
    }
}
