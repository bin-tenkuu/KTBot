package my.ktbot.utils

class DiceResult(
	/**总数*/
	size: Int,
	/**最大值*/
	max: Int,
) {

	@JvmField
	val max = max.coerceAtLeast(1)

	@JvmField
	val size: Int = when {
		size < 1 -> 1
		size > 99 -> 99
		else -> size
	}

	@JvmField
	var sum: Long = this.size.toLong()

	@JvmField
	val list = IntArray(this.size) { 1 }

	constructor(
		sum: Long,
		/**列表*/
		list: IntArray,
		max: Int,
	) : this(list.size, max) {
		this.sum = sum
		System.arraycopy(list, 0, this.list, 0, size)
	}

	val origin get() = "${list.size}d${max}"

	fun dice(): DiceResult {
		val range = 1..when {
			max < 1 -> return this
			max > 999_999_999 -> 999_999_999
			else -> max
		}
		var sum: Long = 0
		for (i in 0..list.size) {
			range.random().also {
				list[i] = it
				sum += it
			}
		}
		this.sum = sum
		return this
	}

	operator fun plus(dice: DiceResult): DiceResult {
		return DiceResult(
			sum = sum + dice.sum,
			max = if (max == dice.max) max else error("max 需要相同"),
			list = intArrayOf(*list, *dice.list)
		)
	}

	override fun toString(): String = "<$origin>"
}
