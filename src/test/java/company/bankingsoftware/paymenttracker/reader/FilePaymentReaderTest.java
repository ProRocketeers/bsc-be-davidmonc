package company.bankingsoftware.paymenttracker.reader;

import company.bankingsoftware.paymenttracker.model.Payment;
import company.bankingsoftware.paymenttracker.model.PaymentEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class FilePaymentReaderTest {

    private static final String NOT_EXISTING_FILE = "not-existing-file";
    private static final String EMPTY_FILE = "empty-file";
    private static final String EMPTY_LINES_FILE = "empty-lines-file";
    private static final String VALID_LINES_FILE = "valid-lines-file";
    private static final String INVALID_LINES_FILE = "invalid-lines-file";
    private static final String VALID_LINES_WITH_QUIT_FILE = "valid-lines-with-quit-file";

    private static final String USD_1000 = "USD 1000";
    private static final String US_1000 = "US 1000";

    private FilePaymentReader filePaymentReader;

    @Mock
    private Path pathMock;
    @Mock
    private BlockingQueue<PaymentEvent> inputPaymentEventsQueueMock;
    @Mock
    private PaymentParser paymentParserMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readFileInput_withNonExistingFile_shouldSkipFile() throws ParseException {
        filePaymentReader = new FilePaymentReader(
                Paths.get(NOT_EXISTING_FILE),
                inputPaymentEventsQueueMock,
                paymentParserMock,
                Level.OFF);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.never()).toPayment(Mockito.anyString());
    }

    @Test
    public void readFileInput_withEmptyFile_shouldSkipFile() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        Paths.get(this.getClass().getClassLoader().getResource(EMPTY_FILE).toURI()),
                        inputPaymentEventsQueueMock,
                        paymentParserMock,
                        Level.OFF);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.never()).toPayment(Mockito.anyString());
    }

    @Test
    public void readFileInput_withEmptyLines_shouldSkipEmptyLines() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        Paths.get(this.getClass().getClassLoader().getResource(EMPTY_LINES_FILE).toURI()),
                        inputPaymentEventsQueueMock,
                        paymentParserMock,
                        Level.OFF);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(2)).toPayment(Mockito.anyString());
    }

    @Test
    public void readFileInput_withValidLines_shouldParseValidLines() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        Paths.get(this.getClass().getClassLoader().getResource(VALID_LINES_FILE).toURI()),
                        inputPaymentEventsQueueMock,
                        paymentParserMock,
                        Level.OFF);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(2)).toPayment(Mockito.anyString());
    }

    @Test
    public void readFileInput_withInvalidLines_shouldParseInvalidLines() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        Paths.get(this.getClass().getClassLoader().getResource(INVALID_LINES_FILE).toURI()),
                        inputPaymentEventsQueueMock,
                        paymentParserMock,
                        Level.OFF);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(5)).toPayment(Mockito.anyString());
    }

    @Test
    public void readFileInput_withQuitLine_shouldParseQuitLineAndNotQuit() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        Paths.get(this.getClass().getClassLoader().getResource(VALID_LINES_WITH_QUIT_FILE).toURI()),
                        inputPaymentEventsQueueMock,
                        paymentParserMock,
                        Level.OFF);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(3)).toPayment(Mockito.anyString());
    }

    @Test
    public void parsePaymentEvent_withValidPaymentEvent_shouldAddPaymentEvent() throws ParseException {
        Mockito.when(paymentParserMock.toPayment(USD_1000)).thenReturn(Payment.builder().build());

        filePaymentReader = new FilePaymentReader(pathMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        PaymentEvent paymentEvent = filePaymentReader.parsePaymentEvent(USD_1000);

        assertThat(paymentEvent, is(notNullValue()));
        assertThat(paymentEvent.getPaymentEventType(), is(PaymentEvent.PaymentEventType.ADD));
    }

    @Test
    public void parsePaymentEvent_withInvalidPaymentEvent_shouldReturnNull() throws ParseException {
        Mockito.when(paymentParserMock.toPayment(US_1000)).thenThrow(ParseException.class);

        filePaymentReader = new FilePaymentReader(pathMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);

        assertThat(filePaymentReader.parsePaymentEvent(US_1000), is(nullValue()));
    }

    @Test
    public void queue_withValidPaymentEvent_shouldInvokePutCall() throws InterruptedException {
        filePaymentReader = new FilePaymentReader(pathMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        filePaymentReader.queue(PaymentEvent.builder().build());

        Mockito.verify(inputPaymentEventsQueueMock, Mockito.times(1)).put(Mockito.any());
    }

    @Test
    public void queue_withNullPaymentEvent_shouldNotInvokePutCall() throws InterruptedException {
        filePaymentReader = new FilePaymentReader(pathMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        filePaymentReader.queue(null);

        Mockito.verify(inputPaymentEventsQueueMock, Mockito.never()).put(Mockito.any());
    }

    @Test
    public void queue_notAbleToPut_shouldNotInvokePutCall() throws InterruptedException {
        Mockito.doThrow(InterruptedException.class).when(inputPaymentEventsQueueMock).put(PaymentEvent.builder().build());

        filePaymentReader = new FilePaymentReader(pathMock, inputPaymentEventsQueueMock, paymentParserMock, Level.OFF);
        filePaymentReader.queue(null);

        Mockito.verify(inputPaymentEventsQueueMock, Mockito.never()).put(Mockito.any());
    }
}
