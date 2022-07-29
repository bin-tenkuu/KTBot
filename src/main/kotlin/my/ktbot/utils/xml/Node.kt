package my.ktbot.utils.xml

import org.xml.sax.Attributes

open class Node(
	@JvmField
	var tag: String,
	attributes: Attributes? = null,
) {
	@JvmField
	val attributes = LinkedHashMap<String, String>(attributes?.length ?: 0)

	@JvmField
	val children = ArrayList<Node>()
	open val isRoot: Boolean = false
	open val isText: Boolean = false

	init {
		if (attributes !== null) for (i in 0 until attributes.length) {
			this.attributes[attributes.getQName(i)] = attributes.getValue(i)
		}
	}

	override fun toString(): String {
		return buildString {
			append("<").append(tag)
			if (attributes.isNotEmpty())
				append(" ...")
			if (children.isEmpty())
				append("/>")
			else append(">...</").append(tag).append(">")
		}
	}

	open fun toXml(): String {
		return buildString {
			append("<").append(tag)
			for ((key, value) in attributes) {
				append(" ").append(key).append("=\"").append(value).append("\"")
			}
			if (children.isEmpty())
				append("/>")
			else {
				append(">")
				for (child in children) {
					append(child.toXml())
				}
				append("</").append(tag).append(">")
			}
		}
	}

	class Root(tag: String, attributes: Attributes? = null) : Node(tag, attributes) {
		override val isRoot: Boolean = true
	}

	class TextNode(
		@JvmField
		val text: String,
	) : Node(TAG, null) {
		@JvmField
		var cddata: Boolean = checkCDDATA(text)

		companion object {
			const val TAG = "<text/>"
			private val needCDDATA = Regex("[<&]")
			fun checkCDDATA(text: String) = needCDDATA.containsMatchIn(text)
		}

		override fun toString(): String = text

		override fun toXml() = if (cddata) "<![CDATA[$text]]>" else text
		override fun equals(other: Any?): Boolean {
			return when {
				this === other -> true
				other !is TextNode -> false
				else -> text == other.text
			}
		}

		override fun hashCode(): Int {
			return text.hashCode()
		}

		override val isText = true
	}

}
