package my.ktbot.annotation

import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER

@Target(FUNCTION, PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class AutoCall(val weight: Int = 0)
