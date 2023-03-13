package my.ktbot.ktor.dao

import org.ktorm.entity.Entity
import org.ktorm.ksp.api.PrimaryKey
import org.ktorm.ksp.api.Table

/**
 *  @Date:2023/3/13
 *  @author bin
 *  @version 1.0.0
 */
@Table(tableName = "HisMsg", tableClassName = "THisMsg", alias = "hm")
interface HisMsg : Entity<HisMsg> {
    @PrimaryKey
    var id: Long
    var type: String
    var msg: String
    var role: String
}

@Table(tableName = "Role", tableClassName = "TRole", alias = "r")
interface Role : Entity<Role> {
    @PrimaryKey
    var name: String
    var tags: String
}
