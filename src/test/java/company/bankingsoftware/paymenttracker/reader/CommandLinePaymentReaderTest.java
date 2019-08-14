package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.*;

public class CommandLinePaymentReaderTest {

    private static final String QUIT_COMMAND = "quit";
    private static final String USD_1000 = "USD 1000";
    private static final String US_1000 = "US 1000";

    private CommandLinePaymentReader commandLinePaymentReader;

    @Mock
    private PaymentParser paymentParserMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readCommandLineIput_withQuitCommand_shouldNotParseAnythingAndShouldQuit() {
        InputStream inputStream = new ByteArrayInputStream(QUIT_COMMAND.getBytes());

        commandLinePaymentReader = new CommandLinePaymentReader(paymentParserMock, inputStream);
        commandLinePaymentReader.readCommandLineInput();

        Mockito.verify(paymentParserMock, Mockito.never()).toPayment(QUIT_COMMAND);
    }

    @Test
    public void readCommandLineInput_withValidPaymentAndQuitCommand_shouldParsePaymentAndShouldQuit() {
        when(paymentParserMock.toPayment(USD_1000)).thenReturn(Payment.builder().build());
        InputStream inputStream = new ByteArrayInputStream(
                String.join(System.lineSeparator(), USD_1000, QUIT_COMMAND).getBytes()
        );

        commandLinePaymentReader = new CommandLinePaymentReader(paymentParserMock, inputStream);
        commandLinePaymentReader.readCommandLineInput();

        Mockito.verify(paymentParserMock, Mockito.times(1)).toPayment(USD_1000);
    }

    @Test
    public void readCommandLineInput_withInvalidPaymentAndQuitCommand_shouldParsePaymentAndShouldQuit() {
        // This behavior is based on assumption
        when(paymentParserMock.toPayment(US_1000)).thenReturn(Payment.builder().build());
        InputStream inputStream = new ByteArrayInputStream(
                String.join(System.lineSeparator(), US_1000, QUIT_COMMAND).getBytes()
        );

        commandLinePaymentReader = new CommandLinePaymentReader(paymentParserMock, inputStream);
        commandLinePaymentReader.readCommandLineInput();

        Mockito.verify(paymentParserMock, Mockito.times(1)).toPayment(US_1000);
    }
}
