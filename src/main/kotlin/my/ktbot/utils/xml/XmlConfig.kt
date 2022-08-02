package my.ktbot.utils.xml

class XmlConfig {
	var version: String? = null
	var encoding: String? = null
	var standalone: Boolean? = null
	var standaloneString: String?
		get() = when (standalone) {
			null -> null
			true -> "yes"
			false -> "no"
		}
		set(value) {
			when {
				value === null -> standalone = null
				"no".equals(value, true) -> standalone = false
				"yes".equals(value, true) -> standalone = true
			}
		}
	var root: Node.Root = Node.Root("root")

	override fun toString(): String {
		return buildString {
			append("<?xml version=\"").append(version).append("\"")
			if (encoding !== null) {
				append(" encoding=\"").append(encoding).append("\"")
			}
			if (standaloneString !== null) {
				append(" standalone=\"").append(standaloneString).append("\"")
			}
			append("?>")
		}
	}

	fun toXml(): String {
		return StringBuilder().append(toString()).append(root.toXml()).toString()
	}
}
