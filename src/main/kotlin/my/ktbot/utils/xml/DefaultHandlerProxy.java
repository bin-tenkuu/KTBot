package my.ktbot.utils.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author bin
 */
public class DefaultHandlerProxy extends DefaultHandler {
    /**
     * Receives notification of the XML declaration.
     * <p>
     * why should my must add this file
     *
     * @param version the version string as in the input document, null if not
     * specified
     * @param encoding the encoding string as in the input document, null if not
     * specified
     * @param standalone the standalone string as in the input document, null if
     * not specified
     * @throws SAXException if the application wants to report an error or
     * interrupt the parsing process
     * @implSpec The default implementation in the SAX API is to do nothing.
     * @since 14
     */
    @Override
    public void declaration(final String version, final String encoding, final String standalone) throws SAXException {
        super.declaration(version, encoding, standalone);
    }
}
