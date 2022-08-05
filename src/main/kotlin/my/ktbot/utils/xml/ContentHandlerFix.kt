package my.ktbot.utils.xml

import org.xml.sax.SAXException

interface ContentHandlerFix {
	/**
	 * Receives notification of the XML declaration.
	 *
	 * why should my must add this file
	 *
	 * @param version the version string as in the input document, null if not specified
	 * @param encoding the encoding string as in the input document, null if not specified
	 * @param standalone the standalone string as in the input document, null if not specified
	 * @throws SAXException if the application wants to report an error or interrupt the parsing process
	 * @implSpec The default implementation in the SAX API is to do nothing.
	 * @since 14
	 */
	@Throws(SAXException::class)
	fun declaration(version: String?, encoding: String?, standalone: String?)
}
