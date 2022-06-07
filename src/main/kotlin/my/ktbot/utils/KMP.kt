package my.ktbot.utils

/**
 *  @Date:2022/6/7
 *  @author bin
 *  @version 1.0.0
 */
object KMP {
	@JvmStatic
	fun <T : Comparable<T>> search(list: List<T>, sublist: List<T>): Int {
		val nextMap = toNextMap(sublist)
		var i = 0
		var j = 0
		while (i < list.size) {
			if (list[i] == sublist[j]) {
				i++
				j++
				if (j == sublist.size) {
					return i - j
				}
				continue
			}
			j = nextMap[j]
			if (j == 0) {
				i++
			}
		}
		return -1
	}

	@JvmStatic
	fun <T : Comparable<T>> search(list: Array<T>, sublist: Array<T>): Int {
		val nextMap = toNextMap(sublist)
		var i = 0
		var j = 0
		while (i < list.size) {
			if (list[i] == sublist[j]) {
				i++
				j++
				if (j == sublist.size) {
					return i - j
				}
				continue
			}
			j = nextMap[j]
			if (j == 0) {
				i++
			}
		}
		return -1
	}

	private fun <T : Comparable<T>> toNextMap(l: List<T>): IntArray {
		val array = IntArray(l.size)
		var i = 1
		var last = 0
		while (i < l.size) {
			if (l[last] == l[i]) {
				array[i] = last + 1
				i++
				last++
				continue
			}
			last = array[last]
			if (last == 0) {
				i++
				continue
			}
		}
		return array
	}

	private fun <T : Comparable<T>> toNextMap(l: Array<T>): IntArray {
		val array = IntArray(l.size)
		var i = 1
		var last = 0
		while (i < l.size) {
			if (l[last] == l[i]) {
				array[i] = last + 1
				i++
				last++
				continue
			}
			last = array[last]
			if (last == 0) {
				i++
				continue
			}
		}
		return array
	}

	@JvmStatic
	fun main(args: Array<String>) {
		val list = listOf(1, 2, 3, 1, 2, 1, 2, 3, 1, 2)
		val sublist = listOf(1, 2, 1, 2, 3, 1, 2)
		println(search(list, sublist))
	}
}
