package my.ktbot.utils.xml

import org.xml.sax.*
import org.xml.sax.helpers.DefaultHandler

class XmlHandler : DefaultHandler(), DefaultHandlerFix {
	val xmlConf: XmlConfig = XmlConfig()
	private var parsed: Boolean? = null

	private val deque = ArrayDeque<Node>()
	private lateinit var lastNode: Node
	private var locator: Locator? = null
	override fun setDocumentLocator(locator: Locator) {
		this.locator = locator
	}

	override fun declaration(version: String?, encoding: String?, standalone: String?) {
		xmlConf.version = version
		xmlConf.encoding = encoding
		xmlConf.standaloneString = standalone
	}

	override fun startDocument() {
		parsed = false
	}

	override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
		val node: Node
		deque.lastOrNull().also {
			if (it === null) {
				node = Node.Root(qName, attributes)
				xmlConf.root = node
			}
			else {
				node = Node(qName, attributes)
				it.children += node
			}
		}
		deque.addLast(node)
	}

	override fun characters(ch: CharArray, start: Int, length: Int) {
		val trim = String(ch, start, length).trim()
		if (trim.isNotEmpty()) {
			deque.last().children += Node.TextNode(trim)
		}
	}

	override fun endElement(uri: String?, localName: String?, qName: String) {
		lastNode = deque.removeLast()
	}

	override fun endDocument() {
		parsed = true
	}

	override fun warning(e: SAXParseException) {
		throw e
	}

	override fun error(e: SAXParseException) {
		throw e
	}

	override fun fatalError(e: SAXParseException) {
		throw e
	}

	override fun resolveEntity(publicId: String?, systemId: String?): InputSource {
		"resolveEntity".pl()
		return super.resolveEntity(publicId, systemId)
	}

	override fun notationDecl(name: String?, publicId: String?, systemId: String?) {
		"notationDecl".pl()
		super.notationDecl(name, publicId, systemId)
	}

	override fun unparsedEntityDecl(name: String?, publicId: String?, systemId: String?, notationName: String?) {
		"notationDecl".pl()
		super.unparsedEntityDecl(name, publicId, systemId, notationName)
	}

	override fun startPrefixMapping(prefix: String?, uri: String?) {
		"startPrefixMapping".pl()
		super.startPrefixMapping(prefix, uri)
	}

	override fun endPrefixMapping(prefix: String?) {
		"endPrefixMapping".pl()
		super.endPrefixMapping(prefix)
	}

	override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
		"ignorableWhitespace".pl()
		super.ignorableWhitespace(ch, start, length)
	}

	override fun processingInstruction(target: String?, data: String?) {
		"processingInstruction".pl()
		super.processingInstruction(target, data)
	}

	override fun skippedEntity(name: String?) {
		"skippedEntity".pl()
		super.skippedEntity(name)
	}

	private fun Any.pl() = println(this)

}

