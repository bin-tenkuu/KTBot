package my

import my.parser.xml.Node
import my.parser.xml.XmlHandler
import javax.xml.parsers.SAXParserFactory

/**
 *  @Date:2022/5/1
 *  @author bin
 *  @version 1.0.0
 */
object XmlParser : Print {
	@JvmStatic
	fun main(vararg args: String) {
		val instance = SAXParserFactory.newInstance()
		val xmlReader = instance.newSAXParser()
		val xmlHandler = XmlHandler()
		XmlParser::class.java.classLoader.getResourceAsStream("test.xml")?.let {
			xmlReader.parse(it, xmlHandler)
		}
		Node.printTree(xmlHandler.result ?: kotlin.run {
			"没有识别到".pl()
			return
		})
	}

}

