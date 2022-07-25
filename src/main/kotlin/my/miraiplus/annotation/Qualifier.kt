package my.miraiplus.annotation

/**
 * 根据
 * @property name [String]
 * @constructor
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
@MustBeDocumented
annotation class Qualifier(val name: String)
