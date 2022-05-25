package my.ktbot.database

import org.ktorm.entity.Entity
import org.ktorm.schema.long

/**
 *
 * @author bin
 * @since 2022/1/6
 */

object TGroup : TGmt<Group>("tb_group") {
	val invited = long("invited").bindTo { it.invited }
}

interface Group : Gmt<Group> {
	var name: String
	var invited: Long

	companion object : Entity.Factory<Group>()
}
