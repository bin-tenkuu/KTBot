package my.ktbot.utils

/**
 * 查找算法
 * @date 2022/6/7
 * @author bin
 * @version 1.0.0
 */
object KMP {
    @JvmStatic
    fun <T : Comparable<T>> search(list: List<T>, sublist: List<T>): Int {
        if (list.isEmpty() || sublist.isEmpty() || list.size < sublist.size) {
            return -1
        }
        if (sublist.size == 1) {
            return list.indexOf(sublist[0])
        }
        val nextMap = toNextMap(sublist)
        var iList = 0
        var iSub = 0
        while (iList < list.size) {
            if (list[iList] == sublist[iSub]) {
                iList++
                iSub++
                if (iSub == sublist.size) {
                    return iList - iSub
                }
                continue
            }
            iSub = nextMap[iSub]
            if (iSub == 0) {
                iList++
            }
        }
        return -1
    }

    /**
     *
     * @param T Comparable<*>
     * @param l List<T>
     * @return IntArray
     */
    private fun <T : Comparable<T>> toNextMap(l: List<T>): IntArray {
        val array = IntArray(l.size)
        // 当前查找的下标
        var i = 1
        // 上一个匹配的位置
        var last = 0
        // 查找与当前下标中的值相同的值的位置
        while (i < l.size) {
            // 如果匹配到了，那么查找时只要从 [last + 1] 位置继续查找即可
            if (l[last] == l[i]) {
                array[i] = last + 1
                i++
                last++
                continue
            }
            // 如果没有匹配到，那么就从上一次匹配到的位置的位置开始查找
            last = array[last]
            // 如果没有上一次匹配到的位置，那么开始查找下一个位置
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
