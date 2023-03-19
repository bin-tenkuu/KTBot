package my.ktbot.annotation

import my.ktbot.PlugConfig
import my.miraiplus.Caller
import my.miraiplus.ArgsMap
import my.miraiplus.Injector

/**
 *  @Date:2022/5/28
 *  @author bin
 *  @version 1.0.0
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class NeedAdmin {
    companion object Inject : Injector.Message<NeedAdmin> {
        override val weight: Double
            get() = -10.0

        override suspend fun doBefore(ann: NeedAdmin, tmpMap: ArgsMap, caller: Caller): Boolean {
            val event =  tmpMap[event] ?: return false
            return PlugConfig.isAdmin(event)
        }
    }
}

