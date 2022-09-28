package my.miraiplus.annotation

/**
 * 从 [my.miraiplus.ArgsMap] 中获取 [name] 相同的对象
 * @property name [String] 对象别名
 * @constructor
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention
@MustBeDocumented
annotation class Qualifier(val name: String)
