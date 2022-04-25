package my.ktbot.utils.calculator

import my.ktbot.utils.DiceResult
import java.util.*
import kotlin.math.pow

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
	private val operatorMap = mutableMapOf<String, Pair<Int, IOperator2>>(
		"d" to (20 to { l, r -> DiceNode(DiceResult.dice(l.v.toInt(), r.v.toInt())) }),
		"**" to (16 to { l, r -> NumberNode(l.v.toDouble().pow(r.v.toDouble())) }),
		"*" to (15 to { l, r -> NumberNode(l.v.toDouble() * r.v.toDouble()) }),
		"/" to (15 to { l, r -> NumberNode(l.v.toDouble() / r.v.toDouble()) }),
		"%" to (15 to { l, r -> NumberNode(l.v.toDouble() % r.v.toDouble()) }),
		"+" to (14 to { l, r -> NumberNode(l.v.toDouble() + r.v.toDouble()) }),
		"-" to (14 to { l, r -> NumberNode(l.v.toDouble() - r.v.toDouble()) }),
		"<<" to (11 to { l, r -> NumberNode(l.v.toLong() shl r.v.toInt()) }),
		">>" to (11 to { l, r -> NumberNode(l.v.toLong() shr r.v.toInt()) }),
		"&" to (8 to { l, r -> NumberNode(l.v.toLong() and r.v.toLong()) }),
		"^" to (7 to { l, r -> NumberNode(l.v.toLong() xor r.v.toLong()) }),
		"|" to (6 to { l, r -> NumberNode(l.v.toLong() or r.v.toLong()) }),
	)

	/**一元运算符*/
	@JvmStatic
	private val operatorMapSingle = mutableMapOf<String, Pair<Int, IOperator1>>(
		"+" to (15 to { l -> l }),
		"-" to (15 to { l -> NumberNode(-l.v.toDouble()) }),
		"~" to (15 to { l -> NumberNode(l.v.toLong().inv()) }),
	)

	/**入口*/
	@JvmStatic
	operator fun invoke(string: String): Calc {
		return toInfixExpressionList(split(string)).toSuffixExpression()
	}

	/**切分字符串*/
	@JvmStatic
	private fun split(string: String) = split.split(string)

	/**
	 * 字符串列表转中缀表达式
	 * @throws IllegalArgumentException 如果 输入为空/数字不正确/未定义的符号/结束括号未匹配
	 */
	@JvmStatic
	private tailrec fun toInfixExpressionList(list: List<String>): Stack<Node> {
		val sb = StringBuilder()
		val stack = Stack<Node>()
		if (list.isEmpty()) throw IllegalArgumentException("输入为空")
		var single = true
		val iterator = list.iterator()
		while (iterator.hasNext()) {
			val it = iterator.next()
			// 数字
			if (it[0] in numberChar) {
				if (sb.isNotEmpty()) {
					stack.push(toOperator(sb.toString(), single))
					sb.clear()
				}
				stack.push(NumberNode(it.toDoubleOrNull() ?: throw IllegalArgumentException("数字不正确：${it}")))
				single = false
			}
			// 算数
			else if (it[0] !in symbolChar) sb.append(it)
			// 括号
			else {
				if (sb.isNotEmpty()) {
					stack.push(toOperator(sb.toString(), single))
					sb.clear()
				}
				if (it == ")") {
					throw IllegalArgumentException("未匹配的结束括号")
				}
				stack.push(StringNode(iterator.nextPart()))
				single = false
			}
		}
		val node = stack[0]
		if (stack.size == 1 && node is StringNode) return toInfixExpressionList(node.s)
		return stack
	}

	@JvmStatic
	private fun Stack<Node>.toSuffixExpression(): Calc {
		//创建一个栈用于保存操作符
		val opStack = Stack<Operator>()
		val stack = Calc()
		val iterator = iterator()
		while (iterator.hasNext()) when (val it = iterator.next()) {
			is NumberNode -> stack += it
			is Operator -> {
				while (opStack.isNotEmpty() && it.priority <= opStack.peek().priority) stack += opStack.pop()
				opStack.push(it)
			}
			is StringNode -> stack += toInfixExpressionList(it.s).toSuffixExpression().toData()
		}
		while (opStack.isNotEmpty()) stack += opStack.pop()
		return stack
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
	private fun toOperator(sb: String, single: Boolean): Operator {
		val operator = if (single) {
			val pair = operatorMapSingle[sb] ?: throw IllegalArgumentException("未定义的单元符号：${sb}")
			Operator1(sb, pair.first, pair.second)
		}
		else {
			val pair = operatorMap[sb] ?: throw IllegalArgumentException("未定义的二元符号：${sb}")
			Operator2(sb, pair.first, pair.second)
		}
		return operator
	}

}
