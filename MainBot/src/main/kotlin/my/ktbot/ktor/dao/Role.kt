package my.ktbot.ktor.dao

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

@Table(tableName = "Role", tableClassName = "TRole", alias = "r")
interface Role : Entity<Role> {
    @PrimaryKey
    var id: String
    var name: String
    var tags: String
}
