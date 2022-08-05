package my.ktbot.utils.xml

open class XmlEntity : ToXml {
	@JvmField
	var declaration: XmlConfig? = null

	@JvmField
	var root: Node.Root = Node.Root("root")

	fun declaration(version: String? = null, encoding: String? = null, standalone: Boolean? = null): XmlEntity {
		declaration = XmlConfig(version, encoding, standalone)
		return this
	}

	inline fun declaration(block: XmlConfig.() -> Unit): XmlEntity {
		declaration = (declaration ?: XmlConfig(standalone = null)).apply(block)
		return this
	}

	fun root(tag: String): Node.Root {
		root.tag = tag
		return root
	}

	fun root(tag: String, vararg pairs: Pair<String, String>): Node.Root {
		root.tag = tag
		root.attributes.putAll(pairs)
		return root
	}

	inline fun root(tag: String, block: Node.Root.() -> Unit): Node.Root {
		root.tag = tag
		root.block()
		return root
	}

	fun Node.item(tag: String): Node {
		val node = Node(tag)
		children.add(node)
		return node
	}

	fun Node.item(tag: String, vararg pairs: Pair<String, String>): Node {
		val node = Node(tag)
		children.add(node)
		node.attributes.putAll(pairs)
		return node
	}

	inline fun Node.item(tag: String, block: Node.() -> Unit): Node {
		val node = Node(tag)
		children.add(node)
		node.block()
		return node
	}

	fun Node.attributes(vararg pairs: Pair<String, String>): Node {
		attributes.putAll(pairs)
		return this
	}

	fun Node.text(text: String) {
		children.add(Node.TextNode(text))
	}

	companion object {
		inline operator fun invoke(block: XmlEntity.() -> Unit): XmlEntity {
			return XmlEntity().apply(block)
		}

		inline operator fun invoke(
			version: String? = null,
			encoding: String? = null,
			standalone: Boolean? = null,
			block: XmlEntity.() -> Unit,
		): XmlEntity {
			val buildXml = XmlEntity().declaration(version, encoding, standalone)
			return buildXml.apply(block)
		}
	}

	override fun toXml(): String {
		return buildString {
			declaration?.run {
				append(toXml())
			}
			root.run {
				append(toXml())
			}
		}
	}
}
