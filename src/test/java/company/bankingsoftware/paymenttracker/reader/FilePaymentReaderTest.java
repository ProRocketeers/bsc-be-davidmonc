package company.bankingsoftware.paymenttracker.reader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;

public class FilePaymentReaderTest {

    private static final String NOT_EXISTING_FILE = "not-existing-file";
    private static final String EMPTY_FILE = "empty-file";
    private static final String EMPTY_LINES_FILE = "empty-lines-file";
    private static final String VALID_LINES_FILE = "valid-lines-file";
    private static final String INVALID_LINES_FILE = "invalid-lines-file";
    private static final String VALID_LINES_WITH_QUIT_FILE = "valid-lines-with-quit-file";

    private FilePaymentReader filePaymentReader;

    @Mock
    private PaymentParser paymentParserMock;
    @Mock
    private PrintStream errorStreamMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readFileInput_withNonExistingFile_shouldSkipFile() throws ParseException {
        filePaymentReader = new FilePaymentReader(paymentParserMock, Paths.get(NOT_EXISTING_FILE), errorStreamMock);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.never()).toPayment(Mockito.anyString());
        Mockito.verify(errorStreamMock).println(Mockito.anyString());
        Mockito.verify(errorStreamMock).flush();
    }

    @Test
    public void readFileInput_withEmptyFile_shouldSkipFile() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        paymentParserMock,
                        Paths.get(this.getClass().getClassLoader().getResource(EMPTY_FILE).toURI()),
                        errorStreamMock);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.never()).toPayment(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).println(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).flush();
    }

    @Test
    public void readFileInput_withEmptyLines_shouldSkipEmptyLines() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        paymentParserMock,
                        Paths.get(this.getClass().getClassLoader().getResource(EMPTY_LINES_FILE).toURI()),
                        errorStreamMock);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(2)).toPayment(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).println(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).flush();
    }

    @Test
    public void readFileInput_withValidLines_shouldParseValidLines() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        paymentParserMock,
                        Paths.get(this.getClass().getClassLoader().getResource(VALID_LINES_FILE).toURI()),
                        errorStreamMock);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(2)).toPayment(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).println(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).flush();
    }

    @Test
    public void readFileInput_withInvalidLines_shouldParseInvalidLines() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        paymentParserMock,
                        Paths.get(this.getClass().getClassLoader().getResource(INVALID_LINES_FILE).toURI()),
                        errorStreamMock);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(5)).toPayment(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).println(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).flush();
    }

    @Test
    public void readFileInput_withQuitLine_shouldParseQuitLineAndNotQuit() throws URISyntaxException, ParseException {
        filePaymentReader =
                new FilePaymentReader(
                        paymentParserMock,
                        Paths.get(this.getClass().getClassLoader().getResource(VALID_LINES_WITH_QUIT_FILE).toURI()),
                        errorStreamMock);

        filePaymentReader.readFileInput();

        Mockito.verify(paymentParserMock, Mockito.times(3)).toPayment(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).println(Mockito.anyString());
        Mockito.verify(errorStreamMock, Mockito.never()).flush();
    }
}
