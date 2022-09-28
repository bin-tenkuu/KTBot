package my.ktbot.utils.calculator

class Calc : IValue {
	/**计算结果*/
	override val v: Number get() = deque[0].v
	private val deque = ArrayDeque<IValue>()

	/**计算中产生的其他数据*/
	val list = mutableListOf<Any>()

	@Throws(IllegalArgumentException::class)
	internal operator fun plusAssign(it: Operator) = when (it) {
		is Operator2 -> {
			val pop = deque.removeLast()
			if (deque.isEmpty()) throw IllegalArgumentException("意外的运算符: 「${it.op}」")
			plusAssign(it(deque.removeLast(), pop))
		}
		is Operator1 -> plusAssign(it(deque.removeLast()))
		else -> {
			println(it)
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
