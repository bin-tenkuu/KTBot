package my.ktbot.annotation

import my.ktbot.database.PermCheck
import my.ktbot.database.TPermCheck
import my.ktbot.utils.Sqlite
import my.miraiplus.ArgsMap
import my.miraiplus.Caller
import my.miraiplus.Injector
import my.miraiplus.annotation.MiraiEventHandle
import net.mamoe.mirai.event.events.GroupEvent
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.removeIf

object CheckPerm : Injector<MiraiEventHandle, GroupEvent> {
    override val event = GroupEvent::class

    private val map = HashMap<Long, HashSet<String>>()

    init {
        for (check in Sqlite[TPermCheck]) {
            map.getOrPut(check.id) { HashSet() }.add(check.name)
        }
    }

    override suspend fun doBefore(ann: MiraiEventHandle, tmpMap: ArgsMap, caller: Caller): Boolean {
        val event = tmpMap[event] ?: return false
        return check(event.group.id, caller.name)
    }

    /**
     *
     * @param group Long 使用 [Caller.name]
     * @param name String
     * @return Boolean
     */
    fun check(group: Long, name: String): Boolean {
        val set = map[group] ?: return true
        return name !in set
    }

    fun open(group: Long, name: String) {
        val set = map[group] ?: return
        set -= name
        Sqlite[TPermCheck].removeIf { (it.id eq group).and(it.name eq name) }
    }

    fun close(group: Long, name: String) {
        map.getOrPut(group) { HashSet() }.add(name)
        Sqlite[TPermCheck].add(PermCheck {
            this.id = group
            this.name = name
        })
    }
}


