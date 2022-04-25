package my.ktbot.utils.calculator

import my.ktbot.utils.println
import java.util.*

class Calc : IValue {
	/**计算结果*/
	override val v: Number get() = stack[0].v
	private val stack = Stack<IValue>()

	/**计算中产生的其他数据*/
	val list = mutableListOf<Any>()
	internal operator fun plusAssign(it: Operator) = when (it) {
		is Operator2 -> {
			val pop = stack.pop()
			plusAssign(it.v(stack.pop(), pop))
		}
		is Operator1 -> plusAssign(it.v(stack.pop()))
		else -> {
			it.println()
			error("未受支持的运算符种类")
		}
	}

	operator fun plusAssign(it: IValue) {
		when (val v = stack.push(it)) {
			is Calc -> list.addAll(v.list)
			is DataNode -> list.addAll(v.list)
			is DiceNode -> list.add(v.d)
		}
	}

	internal fun toData() = DataNode(v, list)
}
