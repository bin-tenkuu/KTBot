package my.ktbot.utils.xml

class XmlConfig(
	@JvmField
	var version: String? = null,
	@JvmField
	var encoding: String? = null,
	@JvmField
	var standalone: Boolean? = null,
) : ToXml {
	constructor(version: String? = null, encoding: String? = null, standaloneString: String? = null) : this(
		version, encoding, when {
			"no".equals(standaloneString, true) -> false
			"yes".equals(standaloneString, true) -> true
			else -> null
		}
	)

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

	override fun toXml(): String {
		return toString()
	}
}
