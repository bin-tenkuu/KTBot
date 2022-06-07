package my

/**
 *  @Date:2022/6/7
 *  @author bin
 *  @version 1.0.0
 */
interface Print {
	fun <T : Any?> T.pl() = println(this)
	fun <T : Any?> T.p() = print(this)
}
