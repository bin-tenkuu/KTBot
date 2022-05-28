package my.miraiplus.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
@MustBeDocumented
annotation class Qualifier(val name: String)
