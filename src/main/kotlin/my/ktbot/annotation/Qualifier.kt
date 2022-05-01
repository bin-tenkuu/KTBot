package my.ktbot.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
@MustBeDocumented
annotation class Qualifier(val name: String)
