package my.ktbot.utils.xml

import org.xml.sax.ErrorHandler
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException

interface SaxErrorHandler : ErrorHandler {
	/**
	 * @description 接收不可恢复的错误的通知。
	 * @param e
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun warning(e: SAXParseException) {
		throw e
	}

	/**
	 * @description 接收可恢复的错误的通知
	 * @param e
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun error(e: SAXParseException) {
		throw e
	}

	/**
	 * @description 接收不可恢复的错误的通知。
	 * @param e
	 * @throws SAXException
	 * @author liuquan
	 * @date  2015年12月11日
	 */
	@Throws(SAXException::class)
	override fun fatalError(e: SAXParseException) {
		throw e
	}

}
