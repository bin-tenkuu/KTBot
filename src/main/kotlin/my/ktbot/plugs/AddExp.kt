package my.ktbot.plugs

import my.ktbot.annotation.NeedExp
import my.miraiplus.annotation.MessageHandle
import net.mamoe.mirai.event.EventPriority

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/10
 */
object AddExp {
	@MessageHandle("经验增加", priority = EventPriority.LOWEST)
	@NeedExp(0.5, 1.0)
	@JvmStatic
	private fun invoke() {
	}

}
