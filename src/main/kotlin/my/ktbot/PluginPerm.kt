package my.ktbot

import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermitteeId

object PluginPerm {
	@Suppress("UNCHECKED_CAST")
	val instance: PermissionService<Permission> = PermissionService.INSTANCE as PermissionService<Permission>
	val root: Permission = instance.rootPermission
	val setu: Permission = instance.register(PluginMain.permissionId("setu"), "色图调用权限")

	operator fun Permission.minusAssign(permitteeId: PermitteeId) {
		if (permitteeId in this) {
			instance.cancel(permitteeId, this, false)
		}
	}

	operator fun Permission.plusAssign(permitteeId: PermitteeId) {
		instance.permit(permitteeId, this)
	}

	operator fun PermitteeId.contains(permission: Permission): Boolean {
		return instance.testPermission(this, permission)
	}

	operator fun Permission.contains(permission: PermitteeId): Boolean {
		return instance.testPermission(permission, this)
	}

	operator fun get(name: String): Permission? {
		return instance[PluginMain.permissionId(name)]
	}
}
