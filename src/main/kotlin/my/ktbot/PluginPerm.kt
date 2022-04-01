package my.ktbot

import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService

object PluginPerm {
	val test1: Permission = PermissionService.INSTANCE.register(PluginMain.permissionId("1"), "test1")

}
