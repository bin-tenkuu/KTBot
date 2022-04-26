package my.ktbot.annotation

import kotlin.annotation.AnnotationTarget.*

@Target(FUNCTION, PROPERTY_GETTER)
@Retention
@MustBeDocumented
annotation class AutoCall(val weight: Int = 0)

@Target(VALUE_PARAMETER)
@Retention
@MustBeDocumented
annotation class Qualifier(val value: String)
