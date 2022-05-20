import org.xml.sax.*
import org.xml.sax.helpers.DefaultHandler
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.xml.parsers.SAXParserFactory

/**
 *  @Date:2022/5/1
 *  @author bin
 *  @version 1.0.0
 */
fun main() {
	val instance = SAXParserFactory.newInstance()
	val xmlReader = instance.newSAXParser()
	xmlReader.parse("src/test/resources/test.xml", Handler())
}

class Handler : DefaultHandler() {
	private var indent = 0
	private var version: String? = null
	private lateinit var encoding: Charset
	private var standalone: String? = null

	override fun declaration(version: String, encoding: String?, standalone: String?) {
		super.declaration(version, encoding, standalone)
		this.version = version
		this.encoding = if (encoding === null) StandardCharsets.UTF_8 else Charset.forName(encoding)
		this.standalone = standalone
	}

	override fun startDocument() {
		super.startDocument()
		println("开始解析")
	}

	override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
		super.startElement(uri, localName, qName, attributes)
		indent()
		indent++
		"<$qName".print()
		for (i in 0 until attributes.length) {
			" ".print()
			attributes.getQName(i).print()
			// "|".print()
			// attributes.getLocalName(i).print()
			"=".print()
			'"'.print()
			attributes.getValue(i).print()
			'"'.print()
			// "(${attributes.getType(i)})".print()
		}
		println(">")
	}

	override fun characters(ch: CharArray, start: Int, length: Int) {
		super.characters(ch, start, length)
		val trim = String(ch, start, length)
		if (trim.isNotBlank()) {
			indent()
			trim.print()
			println()
		}
	}

	override fun endElement(uri: String?, localName: String?, qName: String) {
		super.endElement(uri, localName, qName)
		indent--
		indent()
		"</$qName>".print()
		println()
	}

	override fun endDocument() {
		super.endDocument()
		println("结束解析")
	}

	override fun warning(e: SAXParseException?) {
		super.warning(e)
		"warning".print()
	}

	override fun error(e: SAXParseException?) {
		super.error(e)
		"error".print()
	}

	override fun fatalError(e: SAXParseException?) {
		super.fatalError(e)
		"fatalError".print()
	}

	private fun indent() = "\t".repeat(indent).print()
	private inline fun Any.print() = print(this)
}
