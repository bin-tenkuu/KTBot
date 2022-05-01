package my.ktbot.utils.calculator

import my.ktbot.utils.println

class Calc : IValue {
	/**计算结果*/
	override val v: Number get() = deque[0].v
	private val deque = ArrayDeque<IValue>()

	/**计算中产生的其他数据*/
	val list = mutableListOf<Any>()
	internal operator fun plusAssign(it: Operator) = when (it) {
		is Operator2Impl -> {
			val pop = deque.removeLast()
			plusAssign(it.v(deque.removeLast(), pop))
		}
		is Operator1Impl -> plusAssign(it.v(deque.removeLast()))
		else -> {
			it.println()
			error("未受支持的运算符种类")
		}
	}

	operator fun plusAssign(it: IValue) {
		deque.addLast(it)
		when (it) {
			is Calc -> list.addAll(it.list)
			is DataNode -> list.addAll(it.list)
			is DiceNode -> list.add(it.d)
		}
	}

	internal fun toData() = DataNode(v, list)
}
