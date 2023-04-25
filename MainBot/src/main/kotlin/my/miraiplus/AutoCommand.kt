package my.miraiplus

/**
 * @author bin
 * @since 2023/04/25
 */
interface AutoCommand {

    fun run(block: () -> Unit) {

    }

    class Cache {
        @JvmField
        var inited = false
    }
}
