package my.ktbot

import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.console.permission.PermitteeId

object PluginPerm {
	@Suppress("UNCHECKED_CAST")
	val instance: PermissionService<Permission> = PermissionService.INSTANCE as PermissionService<Permission>
	val root: Permission = instance.rootPermission
	val setu: Permission = instance.register(PluginMain.permissionId("setu"), "色图调用权限")

	fun cancel(permitteeId: PermitteeId, permission: Permission, recursive: Boolean = false) {
		instance.cancel(permitteeId, permission, recursive)
	}

	operator fun PermitteeId.minusAssign(permission: Permission) {
		if (permission.testPermission(this)) {
			instance.cancel(this, permission, false)
		}
	}

	operator fun PermitteeId.plusAssign(permission: Permission) {
		instance.permit(this, permission)
	}

	operator fun PermitteeId.contains(permission: Permission): Boolean {
		return instance.testPermission(this, permission)
	}

	operator fun Permission.contains(permission: PermitteeId): Boolean {
		return instance.testPermission(permission, this)
	}

}
