package my.ktbot.utils.xml

class BuildXml {
	val config = XmlConfig()
	val root get() = config.root
	inline fun config(block: XmlConfig.() -> Unit) {
		config.block()
	}

	inline fun root(block: Node.Root.() -> Unit) {
		root.block()
	}

	inline fun root(tag: String, block: Node.Root.() -> Unit) {
		config.root = Node.Root(tag).apply(block)
	}

	fun Node.item(tag: String, block: Node.() -> Unit): Node {
		val node = Node(tag)
		children.add(node)
		node.block()
		return node
	}

	fun Node.text(text: String) {
		children.add(Node.TextNode(text))
	}

	companion object {
		inline operator fun invoke(block: BuildXml.() -> Unit): XmlConfig {
			return BuildXml().apply(block).config
		}
	}
}
