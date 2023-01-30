package my

import my.ktbot.utils.xml.XmlHandler
import org.xml.sax.InputSource
import javax.xml.parsers.SAXParserFactory

/**
 *  @Date:2022/5/1
 *  @author bin
 *  @version 1.0.0
 */
object XmlParser {
	@JvmStatic
	fun main(vararg args: String) {
		val instance = SAXParserFactory.newInstance()
		val xmlReader = instance.newSAXParser()
		val xmlHandler = XmlHandler()
		XmlParser::class.java.classLoader.getResourceAsStream("test.xml")?.let {
			xmlReader.xmlReader.apply {
				contentHandler = xmlHandler
				errorHandler = xmlHandler
			}.parse(InputSource(it))
		}
		xmlHandler.toXml().pl()
	}

}

