package my.ktbot.utils.xml

import org.xml.sax.Attributes

open class Node(
	@JvmField
	val tag: String,
	attributes: Attributes?,
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
		return "<${tag}${if (attributes.isEmpty()) "" else " ..."}${if (children.isEmpty()) "/>" else ">...</${tag}>"}"
	}

	class Root(tag: String, attributes: Attributes) : Node(tag, attributes) {
		override val isRoot: Boolean = true
	}

	class TextNode(
		@JvmField
		val text: String
	) : Node(TAG, null) {
		companion object {
			const val TAG = "<text/>"
		}

		override fun toString(): String = TAG

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

	companion object {
		fun printTree(node: Node, indent: Int = 0) {
			print("\t".repeat(indent))
			if (node is TextNode) println(node.toString())
			else {
				print("<${node.tag}")
				if (node.attributes.isNotEmpty()) node.attributes.forEach { (k, v) ->
					print(" $k=\"$v\"")
				}
				if (node.children.isEmpty()) println("/>")
				else {
					println(">")
					node.children.forEach { printTree(it, indent + 1) }
					print("\t".repeat(indent))
					println("</${node.tag}>")
				}
			}
		}
	}
}
