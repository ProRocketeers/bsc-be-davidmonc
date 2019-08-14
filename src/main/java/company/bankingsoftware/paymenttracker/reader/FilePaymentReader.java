package company.bankingsoftware.paymenttracker.reader;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> paymentParser.toPayment(line));

        } catch (IOException ioe ) {
            errorStream.println(ioe.getLocalizedMessage());
            errorStream.flush();
        }
    }
}
