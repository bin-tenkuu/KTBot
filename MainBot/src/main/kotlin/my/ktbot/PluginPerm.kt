package my.ktbot

import net.mamoe.mirai.console.permission.Permission
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.permission.PermitteeId

object PluginPerm {
    @JvmField
    @Suppress("UNCHECKED_CAST")
    val instance: PermissionService<Permission> = PermissionService.INSTANCE as PermissionService<Permission>

    @JvmField
    val root: Permission = instance.rootPermission

    @JvmField
    val main: Permission = PluginMain.parentPermission

    val admin: Permission = instance.register(PluginMain.permissionId("admin"), "管理员权限")

    @JvmField
    val setu: Permission = instance.register(PluginMain.permissionId("setu"), "色图调用权限")

    @JvmField
    val coc: Permission = instance.register(PluginMain.permissionId("coc"), "coc调用权限")

    @JvmField
    val map = HashMap<String, Permission>()

    operator fun Permission.minusAssign(permitteeId: PermitteeId) {
        if (test(permitteeId, this)) {
            instance.cancel(permitteeId, this, false)
        }
    }

    operator fun Permission.plusAssign(permitteeId: PermitteeId) {
        instance.permit(permitteeId, this)
    }

    fun test(permitteeId: PermitteeId, permission: Permission): Boolean {
        return instance.testPermission(permitteeId, permission)
    }

    fun test(permission: Permission, permitteeId: PermitteeId): Boolean {
        return instance.testPermission(permitteeId, permission)
    }

    operator fun get(name: String): Permission? {
        return instance[PluginMain.permissionId(name)]
    }
}
