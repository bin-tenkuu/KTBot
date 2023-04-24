package my.ktbot.utils.global

/**
 * @author bin
 * @since 2023/04/24
 */
object Debugger {
    @JvmStatic
    var debug = true

    fun <T> yes(t: T, f: T): T {
        return if (debug) t else f
    }

    fun <T> yes(t: T, f: () -> T): T {
        return if (debug) t else f()
    }

    inline fun yes(t: () -> Unit) {
        if (debug) t()
    }

    fun <T> no(t: T, f: T): T {
        return if (!debug) t else f
    }

    fun <T> no(t: T, f: () -> T): T {
        return if (!debug) t else f()
    }

    inline fun no(t: () -> Unit) {
        if (!debug) t()
    }
}
