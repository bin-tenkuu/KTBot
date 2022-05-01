package my.ktbot.utils

class ReplaceNode private constructor(
	private val children: MutableMap<Char, ReplaceNode>,
) {
	constructor() : this(HashMap())

	private var replace: String? = null

	operator fun get(key: Char) = children[key]
	operator fun set(string: String, replace: String) {
		string.fold(this) { node, c ->
			node.children.computeIfAbsent(c) { ReplaceNode() }
		}.replace = replace
	}

	private fun addAll(map: Map<String, String>): ReplaceNode {
		map.forEach(::set)
		return this
	}

	operator fun plus(map: Map<String, String>) = addAll(map)

	fun replace(charSequence: CharSequence): String {
		var node = this
		val sb = StringBuilder(charSequence.length)
		var nodeTmp: ReplaceNode? = null
		var skip = false
		for (c in charSequence) {
			if (skip) {
				sb.append(c)
				skip = false
				continue
			}
			if (c == '\\') {
				if (nodeTmp != null) {
					sb.append(nodeTmp.replace)
					nodeTmp = null
					node = this
				}
				skip = true
				continue
			}
			while (true) {
				val value = node[c]
				if (value != null) {
					if (value.replace != null) nodeTmp = value
					node = value
					break
				}
				if (nodeTmp === null) {
					sb.append(c)
					node = this
					break
				}
				sb.append(nodeTmp.replace)
				nodeTmp = null
				node = this
			}
		}
		if (nodeTmp != null) {
			sb.append(nodeTmp.replace)
		}
		return sb.toString()
	}

}
