package my.ktbot.utils.xml

import org.xml.sax.Attributes
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

class XmlHandler : DefaultHandler() {
	lateinit var xmlConf: XmlConfig
	private var parsed: Boolean? = null

	private val deque = ArrayDeque<Node>()
	private lateinit var lastNode: Node

	var result: Node.Root? = null
		private set

	override fun declaration(version: String, encoding: String?, standalone: String?) {
		xmlConf = XmlConfig(version, encoding, standalone)
		"<?xml version=\"${version}\" encoding=\"${encoding ?: "UTF-8"} standalone=\"${standalone ?: "yes"}\"?>".print()
		println()
	}

	override fun startDocument() {
		parsed = false
	}

	override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
		val node: Node
		deque.lastOrNull().also {
			if (it === null) {
				node = Node.Root(qName, attributes)
				result = node
			} else {
				node = Node(qName, attributes)
				it.children += node
			}
		}
		deque.addLast(node)
	}

	override fun characters(ch: CharArray, start: Int, length: Int) {
		val trim = String(ch, start, length)
		if (trim.isNotBlank()) {
			deque.last().children += Node.TextNode(String(ch, start, length))
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

	@Suppress("NOTHING_TO_INLINE")
	private inline fun Any.print() = print(this)
}

