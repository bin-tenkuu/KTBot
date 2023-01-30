@file:Suppress("NOTHING_TO_INLINE")

package my

/**
 *  @Date:2022/6/7
 *  @author bin
 *  @version 1.0.0
 */
inline fun <T : Any?> T.pl() = println(this)
inline fun <T : Any?> T.p() = print(this)
