package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*

/**
 *
 * @author bin
 * @since 2022/1/6
 */

interface Group : Gmt<Group> {
    var invited: Long

    companion object : Entity.Factory<Group>()
}

object TGroup : Table<Group>(tableName = "qq_group", entityClass = Group::class) {
    val id: Column<Long> = long("id").bindTo { it.id }.primaryKey()
    val exp: Column<Double> = double("exp").bindTo { it.exp }
    val gmtModified: Column<Long> = long("gmt_modified").bindTo { it.gmtModified }
    val gmtCreate: Column<Long> = long("gmt_create").bindTo { it.gmtCreate }
    val isBaned: Column<Boolean> = boolean("is_baned").bindTo { it.isBaned }
    val invited: Column<Long> = long("invited").bindTo { it.invited }
}
