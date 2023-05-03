package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

/**
 *
 * @author bin
 * @since 2022/1/6
 */

interface Group : Gmt<Group> {
    var invited: Long

    companion object : Entity.Factory<Group>()
}

object TGroup : GmtTable<Group>(tableName = "qq_group", entityClass = Group::class) {
    val invited: Column<Long> = long("invited").bindTo { it.invited }
}
