package my.ktbot.plugin.utils

class DiceResult(
	/**总数*/
	val sum: Long,
	/**列表*/
	val list: IntArray,
	/**最大值*/
	val max: Int,
) {
	val origin get() = "${list.size}d${max}"
	operator fun plus(dice: DiceResult): DiceResult {
		return DiceResult(
			sum = sum + dice.sum,
			max = max.coerceAtLeast(dice.max),
			list = list.toMutableList().also {
				it.addAll(dice.list.toList())
			}.toIntArray()
		)
	}

	companion object {
		@JvmStatic
		fun dice(times: Int, max: Int): DiceResult {
			val size = (1..99).between(times)
			val range = 1..when {
				max < 1 -> return DiceResult(size.toLong(), IntArray(size) { 1 }, 1)
				max > 999_999_999 -> 999_999_999
				else -> max
			}
			var sum: Long = 0
			val arr = IntArray(size) {
				range.random().also { sum += it }
			}
			return DiceResult(sum, arr, range.last)
		}

		@JvmStatic
		private fun IntRange.between(v: Int): Int {
			return when {
				isEmpty() -> throw IllegalArgumentException("Cannot get random in empty range: $this")
				v < first -> first
				v > last -> last
				else -> v
			}
		}
	}
}
