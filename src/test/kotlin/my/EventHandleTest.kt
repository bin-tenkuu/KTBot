package my

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.KProperty2
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 *  @Date:2022/6/4
 *  @author bin
 *  @version 1.0.0
 */
object EventHandleTest : Print {
	private object Obj {
		private fun a() = 1
		private val b: Int = 0
		private val String.c get() = this
	}

	private const val preheatCount = 1_000
	private const val execCount = 1_000_000_00

	@JvmStatic
	fun main(args: Array<String>) {
		for (member in Obj::class.declaredMembers) {
			println("开始实验：${member}")
			member.isAccessible = true
			when (member) {
				is KFunction<*> -> {
					val java = member.javaMethod!!
					calc(
						test("\tkotlin:") {
							member.call(Obj)
						}, test("\tjava:") {
							java.invoke(Obj)
						}
					)
				}
				is KProperty1<*, *> -> {
					val java = member.javaField!!
					calc(
						test("\tkotlin:") {
							member.call()
						}, test("\tjava:") {
							java.get(Obj)
						}
					)
				}
				is KProperty2<*, *, *> -> {
					val java = member.javaGetter!!
					calc(
						test("\tkotlin:") {
							member.call(Obj, "kotlin")
						}, test("\tjava:") {
							java.invoke(Obj, "java")
						}
					)
				}
				else -> println("未实验")
			}
		}
	}

	@OptIn(ExperimentalTime::class)
	private inline fun test(token: String, execK: (Int) -> Unit): Duration {
		println("\t开始 $preheatCount 次预热")
		repeat(preheatCount, execK)
		println("\t开始 $execCount 执行")
		print(token)
		val time = measureTime {
			repeat(execCount, execK)
		}
		println(time)
		return time
	}

	private fun calc(k: Duration, j: Duration) {
		print("\tkotlin/java:")
		println(k / j)
	}
}
