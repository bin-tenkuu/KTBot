package my.ktbot.annotation

import my.ktbot.PluginPerm
import my.miraiplus.Caller
import my.miraiplus.ObjectMap
import my.miraiplus.injector.Injector
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent

/**
 *  @Date:2022/6/12
 *  @author bin
 *  @version 1.0.0
 */
annotation class HasPerm(
	@ResolveContext(ResolveContext.Kind.PERMISSION_ID)
	val permId: String,
) {
	object Inject : Injector.Message<HasPerm> {
		private val map = HashMap<String, Permission>()

		override fun doInit(ann: HasPerm, caller: Caller) {
			map[ann.permId] = PluginPerm.instance[PermissionId.parseFromString(ann.permId)]
				?: error("need register before: ${ann.permId}")
		}

		override suspend fun doBefore(
			ann: HasPerm, event: MessageEvent, tmpMap: ObjectMap, caller: Caller,
		): Boolean {
			val permission = map[ann.permId]!!
			return when (event) {
				is GroupMessageEvent -> AbstractPermitteeId.ExactGroup(event.group.id) in permission
				is FriendMessageEvent -> AbstractPermitteeId.ExactUser(event.sender.id) in permission
				else -> false
			}
		}

		private operator fun PermitteeId.contains(permission: Permission): Boolean {
			return PluginPerm.instance.testPermission(this, permission)
		}

		private operator fun Permission.contains(permission: PermitteeId): Boolean {
			return PluginPerm.instance.testPermission(permission, this)
		}


	}
}
