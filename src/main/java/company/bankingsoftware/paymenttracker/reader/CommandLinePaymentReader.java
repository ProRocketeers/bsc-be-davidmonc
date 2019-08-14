package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Scanner;

/**
 * Implementation of PaymentReader, reads the command line input.
 */
public class CommandLinePaymentReader implements PaymentReader {

    private static final String QUIT_COMMAND = "quit";

    private final PaymentParser paymentParser;
    private final InputStream inputStream;
    private final PrintStream errorStream;

    public CommandLinePaymentReader(PaymentParser paymentParser, InputStream inputStream, PrintStream errorStream) {
        this.paymentParser = paymentParser;
        this.inputStream = inputStream;
        this.errorStream = errorStream;
    }

    public void readCommandLineInput() {
        Scanner scanner = new Scanner(inputStream);

        for (String commandLineInput; scanner.hasNext() && !QUIT_COMMAND.equals(commandLineInput = scanner.nextLine());) {
            parsePayment(commandLineInput);
        }
    }

    Payment parsePayment(String paymentLine) {
        try {
            return paymentParser.toPayment(paymentLine);
        } catch (ParseException pe) {
            errorStream.println(pe.getLocalizedMessage());
            errorStream.flush();
        }

        return null;
    }
}
