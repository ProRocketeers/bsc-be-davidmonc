package company.bankingsoftware.paymenttracker.reader;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Implementation of PaymentReader, reads the command line input.
 */
public class CommandLinePaymentReader implements PaymentReader {

    private static final String QUIT_COMMAND = "quit";

    private final PaymentParser paymentParser;
    private final InputStream inputStream;

    public CommandLinePaymentReader(PaymentParser paymentParser, InputStream inputStream) {
        this.paymentParser = paymentParser;
        this.inputStream = inputStream;
    }

    public void readCommandLineInput(){
        Scanner scanner = new Scanner(inputStream);

        for (String commandLineInput; scanner.hasNext() && !QUIT_COMMAND.equals(commandLineInput = scanner.nextLine());) {
            paymentParser.toPayment(commandLineInput);
        }
    }


}
