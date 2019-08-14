package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.stream.Stream;

/**
 * Implementation of PaymentReader, reads the given file as an input.
 */
public class FilePaymentReader implements PaymentReader {

    private final PaymentParser paymentParser;
    private final Path path;
    private final PrintStream errorStream;

    public FilePaymentReader(PaymentParser paymentParser, Path path, PrintStream errorStream) {
        this.paymentParser = paymentParser;
        this.path = path;
        this.errorStream = errorStream;
    }

    public void readFileInput() {
        try (Stream<String> stream = Files.lines(path)) {

            stream
                    .filter(paymentLine -> !paymentLine.isEmpty())
                    .forEach(paymentLine -> parsePayment(paymentLine));

        } catch (IOException ioe ) {
            errorStream.println(ioe.getLocalizedMessage());
            errorStream.flush();
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
