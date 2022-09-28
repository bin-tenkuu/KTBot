package my.ktbot.utils.xml

import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.SAXException

interface SaxContentHandler : ContentHandler {

	/**
	 * 接收用来查找 SAX 文档事件起源的对象。
	 * @param locator 可以返回任何 SAX 文档事件位置的对象
	 */
	override fun setDocumentLocator(locator: Locator) {}

	/**
	 * 接收文档的开始的通知。
	 */
	override fun startDocument() {}

	/**
	 * 接收文档的结尾的通知。
	 * @param uri 元素的命名空间
	 * @param localName ：元素的本地名称（不带前缀）
	 * @param qName 元素的限定名（带前缀）
	 */
	override fun endElement(uri: String?, localName: String?, qName: String) {}

	/**
	 * 接收文档的结尾的通知。
	 */
	override fun endDocument() {}

	/**
	 * 开始前缀 URI 名称空间范围映射。 此事件的信息对于常规的命名空间处理并非必需：
	 * 当 http://xml.org/sax/features/namespaces 功能为 true（默认）时，SAX XML 读取器将自动替换元素和属性名称的前缀。
	 * @param prefix 前缀
	 * @param uri 命名空间
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun startPrefixMapping(prefix: String?, uri: String?) {
		println("startPrefixMapping")
	}

	/**
	 * 结束前缀 URI 范围的映射。
	 * @param prefix
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun endPrefixMapping(prefix: String?) {
		println("endPrefixMapping")
	}


	/**
	 * 接收元素内容中可忽略的空白的通知。
	 * @param ch 来自 XML 文档的字符
	 * @param start  数组中的开始位置
	 * @param length 从数组中读取的字符的个数
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
		println("ignorableWhitespace")
	}

	/**
	 * 接收处理指令的通知。
	 * @param target 处理指令目标
	 * @param data 处理指令数据，如果未提供，则为 null。
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun processingInstruction(target: String?, data: String?) {
		println("processingInstruction")
	}

	/**
	 * 接收跳过的实体的通知。
	 * @param name  所跳过的实体的名称。如果它是参数实体，则名称将以 '%' 开头， 如果它是外部 DTD 子集，则将是字符串 "dtd"
	 * @throws SAXException
	 */
	@Throws(SAXException::class)
	override fun skippedEntity(name: String?) {
		println("skippedEntity")
	}

}
