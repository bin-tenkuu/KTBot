package my.ktbot.utils.xml

import org.xml.sax.Attributes
import java.util.*

class XmlHandler : XmlEntity(), ToXml,//DefaultHandler(),
	ContentHandlerFix, SaxContentHandler, SaxErrorHandler {

	private val deque = LinkedList<Node>()

	override fun declaration(version: String?, encoding: String?, standalone: String?) {
		declaration = XmlConfig(version, encoding, standalone)
	}

	/**
	 * 接收文档的开始的通知。
	 * @param uri 元素的命名空间
	 * @param localName 元素的本地名称（不带前缀）
	 * @param qName 元素的限定名（带前缀）
	 * @param attributes 元素的属性集合
	 */
	override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
		val node: Node
		deque.peekLast().also {
			if (it === null) {
				node = Node.Root(qName, attributes)
				root = node
			}
			else {
				node = Node(qName, attributes)
				it.children += node
			}
		}
		deque.addLast(node)
	}

	/**
	 * 接收字符数据的通知。
	 * 在DOM中 ch[start:end] 相当于Text节点的节点值（nodeValue）
	 * @param ch
	 * @param start
	 * @param length
	 */
	override fun characters(ch: CharArray, start: Int, length: Int) {
		val trim = String(ch, start, length).trim()
		if (trim.isNotEmpty()) {
			deque.peekLast().text(trim)
		}
	}

}

