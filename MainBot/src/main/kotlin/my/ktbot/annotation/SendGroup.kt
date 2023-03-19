package my.ktbot.annotation

import my.ktbot.PluginMain
import my.ktbot.utils.toMessage
import my.miraiplus.Caller
import my.miraiplus.ArgsMap
import my.miraiplus.Injector
import net.mamoe.mirai.event.events.GroupEvent
import net.mamoe.mirai.message.data.isContentBlank
import kotlin.reflect.KClass

/**
 *  @Date:2022/5/31
 *  @author bin
 *  @version 1.0.0
 */
@MustBeDocumented
annotation class SendGroup {
    companion object Inject : Injector<SendGroup, GroupEvent> {
        override val weight: Double
            get() = 1.0
        override val event: KClass<GroupEvent> = GroupEvent::class
        override suspend fun doAfter(ann: SendGroup, event: GroupEvent, tmpMap: ArgsMap, caller: Caller, result: Any?) {
            val message = result.toMessage()
            if (message === null || message.isContentBlank()) {
                return
            }
            event.intercept()
            PluginMain.catch {
                event.group.sendMessage(message)
            } ?: PluginMain.catch {
                event.group.sendMessage("发送失败")
            }
        }
    }
}
