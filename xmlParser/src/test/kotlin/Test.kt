import my.parser.xml.Node
import my.parser.xml.XmlHandler
import javax.xml.parsers.SAXParserFactory

/**
 *  @Date:2022/5/1
 *  @author bin
 *  @version 1.0.0
 */
object Test {
	@JvmStatic
	fun main(vararg args: String) {
		val instance = SAXParserFactory.newInstance()
		val xmlReader = instance.newSAXParser()
		val xmlHandler = XmlHandler()
		Test::class.java.classLoader.getResourceAsStream("test.xml")?.let {
			xmlReader.parse(it, xmlHandler)
		}
		Node.printTree(xmlHandler.result ?: kotlin.run {
			println("没有识别到")
			return
		})
	}

}

