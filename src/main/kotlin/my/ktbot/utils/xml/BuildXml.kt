package my.ktbot.utils.xml


class BuildXml {
	val config = XmlConfig()
	var root
		get() = config.root
		set(value) {
			config.root = value
		}

	fun config(version: String = "1.0", encoding: String? = null, standalone: Boolean? = null): BuildXml {
		config.version = version
		config.encoding = encoding
		config.standalone = standalone
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
		inline operator fun invoke(block: BuildXml.() -> Unit): XmlConfig {
			return BuildXml().apply(block).config
		}

		inline operator fun invoke(
			version: String = "1.0",
			encoding: String? = null,
			standalone: Boolean? = null,
			block: BuildXml.() -> Unit,
		): XmlConfig {
			val buildXml = BuildXml().config(version, encoding, standalone)
			return buildXml.apply(block).config
		}
	}
}
