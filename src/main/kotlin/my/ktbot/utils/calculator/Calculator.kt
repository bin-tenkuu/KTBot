package my.ktbot.utils.calculator

/**
 * [参考链接](https://github.com/killme2008/aviatorscript)
 * @author bin
 * @since 1.0
 * @date 2022/1/18
 * @see Calculator.invoke
 */
object Calculator {
	/**切分格式*/
	@JvmStatic
	private val split = Regex("(?<=[^\\d.])(?=[\\d.])|(?<=[\\d.])(?=[^\\d.])|(?<=[^\\d.])(?=[^\\d.])")

	/**数字*/
	private const val numberChar = ".0123456789"

	/**括号*/
	private const val symbolChar = "()"

	/**二元运算符*/
	@JvmStatic
	private val operatorMap = Operator2Enum.values().associateByTo(HashMap<String, Operator2>()) { it.op }

	/**一元运算符*/
	@JvmStatic
	private val operatorMapSingle = Operator1Enum.values().associateByTo(HashMap<String, Operator1>()) { it.op }

	/**入口*/
	@JvmStatic
	operator fun invoke(string: String): Calc {
		return toSuffixExpression(toInfixExpressionList(split(string)))
	}

	/**切分字符串*/
	@JvmStatic
	private fun split(string: String) = split.split(string)

	/**
	 * 字符串列表转中缀表达式
	 * @throws IllegalArgumentException 如果 输入为空/数字不正确/未定义的符号/结束括号未匹配
	 */
	@JvmStatic
	private tailrec fun toInfixExpressionList(list: List<String>): ArrayDeque<Node> {
		val sb = StringBuilder()
		val deque = ArrayDeque<Node>()
		if (list.isEmpty()) throw IllegalArgumentException("输入为空")
		var single = true
		val iterator = list.iterator()
		while (iterator.hasNext()) {
			val it = iterator.next()
			// 数字
			if (it[0] in numberChar) {
				if (sb.isNotEmpty()) {
					deque.addLast(toOperator(sb.toString(), single))
					sb.clear()
				}
				deque.addLast(NumberNode(it.toDoubleOrNull() ?: throw IllegalArgumentException("数字不正确：${it}")))
				single = false
			}
			// 算数
			else if (it[0] !in symbolChar) sb.append(it)
			// 括号
			else {
				if (sb.isNotEmpty()) {
					deque.addLast(toOperator(sb.toString(), single))
					sb.clear()
				}
				if (it == ")") {
					throw IllegalArgumentException("未匹配的结束括号")
				}
				deque.addLast(StringNode(iterator.nextPart()))
				single = false
			}
		}
		val node = deque.first()
		if (deque.size == 1 && node is StringNode) return toInfixExpressionList(node.s)
		return deque
	}

	@JvmStatic
	private fun toSuffixExpression(nodeDeque: ArrayDeque<Node>): Calc {
		//创建一个栈用于保存操作符
		val opDeque = ArrayDeque<Operator>()
		val calc = Calc()
		val iterator = nodeDeque.iterator()
		while (iterator.hasNext()) when (val it = iterator.next()) {
			is NumberNode -> calc += it
			is Operator -> {
				while (opDeque.isNotEmpty() && it.priority <= opDeque.first().priority) calc += opDeque.removeLast()
				opDeque.addLast(it)
			}
			is StringNode -> calc += toSuffixExpression(this@Calculator.toInfixExpressionList(it.s)).toData()
		}
		while (opDeque.isNotEmpty()) calc += opDeque.removeLast()
		return calc
	}

	/**获取括号内字符串表达式（不包含括号）*/
	@JvmStatic
	private fun Iterator<String>.nextPart(): List<String> {
		val list = mutableListOf<String>()
		var i = 0
		while (hasNext()) {
			val s = next()
			if (s == ")") {
				if (i > 0) i--
				else break
			}
			else if (s == "(") {
				i++
			}
			list.add(s)
		}
		return list
	}

	/**符号转结构体*/
	@JvmStatic
	private fun toOperator(sb: String, single: Boolean): Operator = when (single) {
		true -> operatorMapSingle[sb] ?: throw IllegalArgumentException("未定义的单元符号：${sb}")
		false -> operatorMap[sb] ?: throw IllegalArgumentException("未定义的二元符号：${sb}")
	}

}
