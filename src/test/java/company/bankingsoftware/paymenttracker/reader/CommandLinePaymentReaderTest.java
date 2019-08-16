package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;
import company.bankingsoftware.paymenttracker.model.PaymentEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CommandLinePaymentReaderTest {

    private static final String QUIT_COMMAND = "quit";
    private static final String USD_1000 = "USD 1000";
    private static final String US_1000 = "US 1000";

    private CommandLinePaymentReader commandLinePaymentReader;

    @Mock
    private BlockingQueue<PaymentEvent> inputPaymentEventsQueueMock;
    @Mock
    private PaymentParser paymentParserMock;
    @Mock
    private InputStream inputStreamMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readCommandLineInput_withQuitCommandOnly_shouldNotParseAnythingAndShouldQuit() throws ParseException {
        final InputStream inputStream = new ByteArrayInputStream(QUIT_COMMAND.getBytes());

        commandLinePaymentReader = new CommandLinePaymentReader(inputStream, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        commandLinePaymentReader.readCommandLineInput();

        Mockito.verify(paymentParserMock, Mockito.never()).toPayment(QUIT_COMMAND);
    }

    @Test
    public void readCommandLineInput_withValidPaymentAndQuitCommand_shouldParsePaymentAndShouldQuit() throws ParseException {
        Mockito.when(paymentParserMock.toPayment(USD_1000)).thenReturn(Payment.builder().build());
        final InputStream inputStream = new ByteArrayInputStream(
                String.join(System.lineSeparator(), USD_1000, QUIT_COMMAND).getBytes()
        );

        commandLinePaymentReader = new CommandLinePaymentReader(inputStream, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        commandLinePaymentReader.readCommandLineInput();

        Mockito.verify(paymentParserMock, Mockito.times(1)).toPayment(USD_1000);
    }

    @Test
    public void readCommandLineInput_withInvalidPaymentAndQuitCommand_shouldParsePaymentAndShouldQuit() throws ParseException {
        // This behavior is based on assumption
        final InputStream inputStream = new ByteArrayInputStream(
                String.join(System.lineSeparator(), US_1000, QUIT_COMMAND).getBytes()
        );

        commandLinePaymentReader = new CommandLinePaymentReader(inputStream, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        commandLinePaymentReader.readCommandLineInput();

        Mockito.verify(paymentParserMock, Mockito.times(1)).toPayment(US_1000);
    }

    @Test
    public void parsePaymentEvent_withValidPaymentEvent_shouldAddPaymentEvent() throws ParseException {
        Mockito.when(paymentParserMock.toPayment(USD_1000)).thenReturn(Payment.builder().build());

        commandLinePaymentReader = new CommandLinePaymentReader(inputStreamMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        PaymentEvent paymentEvent = commandLinePaymentReader.parsePaymentEvent(USD_1000);

        assertThat(paymentEvent, is(notNullValue()));
        assertThat(paymentEvent.getPaymentEventType(), is(PaymentEvent.PaymentEventType.ADD));
    }

    @Test
    public void parsePaymentEvent_withInvalidPaymentEvent_shouldReturnNull() throws ParseException {
        Mockito.when(paymentParserMock.toPayment(US_1000)).thenThrow(ParseException.class);

        commandLinePaymentReader = new CommandLinePaymentReader(inputStreamMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);

        assertThat(commandLinePaymentReader.parsePaymentEvent(US_1000), is(nullValue()));
    }

    @Test
    public void queue_withValidPaymentEvent_shouldInvokePutCall() throws InterruptedException {
        commandLinePaymentReader = new CommandLinePaymentReader(inputStreamMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        commandLinePaymentReader.queue(PaymentEvent.builder().build());

        Mockito.verify(inputPaymentEventsQueueMock, Mockito.times(1)).put(Mockito.any());
    }

    @Test
    public void queue_withNullPaymentEvent_shouldNotInvokePutCall() throws InterruptedException {
        commandLinePaymentReader = new CommandLinePaymentReader(inputStreamMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        commandLinePaymentReader.queue(null);

        Mockito.verify(inputPaymentEventsQueueMock, Mockito.never()).put(Mockito.any());
    }
}
