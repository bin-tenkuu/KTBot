package my.parser.xml

class XmlConfig(
	val version: String,
	val encoding: String,
	val standalone: Boolean,
) {
	constructor(
		version: String,
		encoding: String?,
		standalone: String?,
	) : this(
		version,
		encoding ?: "UTF-8",
		"no".equals(standalone, true),
	)
}
