package my.ktbot.annotation

import my.ktbot.PluginPerm
import my.ktbot.PluginPerm.contains
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.permission.PermissionId
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.event.events.UserEvent
import kotlin.reflect.KClass

/**
 *  @Date:2022/6/12
 *  @author bin
 *  @version 1.0.0
 */
annotation class HasPerm(
	@ResolveContext(ResolveContext.Kind.PERMISSION_ID)
	val permId: String,
	val checkUser: Boolean = true,
	val checkGroup: Boolean = true,
) {
	companion object Inject : Injector<HasPerm, BotEvent> {
		override val event: KClass<BotEvent> = BotEvent::class
		override fun doInit(ann: HasPerm, caller: Caller) {
			PluginPerm.map[ann.permId] = PluginPerm.instance[PermissionId.parseFromString(ann.permId)]
				?: error("need register before: ${ann.permId}")
		}

		override suspend fun doBefore(
			ann: HasPerm, event: BotEvent, tmpMap: ObjectMap, caller: Caller,
		): Boolean {
			val permission = PluginPerm.map[ann.permId]!!
			var b = true
			if (ann.checkUser && event is UserEvent) {
				if (event.user.permitteeId in permission) {
					return true
				}
				b = false
			}
			if (ann.checkGroup && event is GroupEvent) {
				if (event.group.permitteeId in permission) {
					return true
				}
				b = false
			}
			return b
		}
	}
}
