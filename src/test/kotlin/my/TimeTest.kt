package my

import java.time.Duration
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

object TimeTest {
	@JvmStatic
	fun main(vararg args: String) {
		fun <T : Any?> T.p() = println(this)
		fun <T : Any?> T.pl() = print(this)
		fun Long.ofMillis(start: Long) = Duration.ofNanos(this - start)
		val obj = object {
			private val a: Int = 0
			private val b: Int get() = 1
			private val String.c: Int get() = length
		}
		val v = obj::class.declaredMembers
		var start: Long
		var end: Long
		val range = 0..1_0000_0000

		for (kCallable in v) {
			kCallable as KProperty<Any>
			kCallable.p()
			kCallable.isAccessible = true
			val param = if (kCallable.parameters.size == 2) arrayOf("abc")
			else arrayOf()

			"kotlin：".pl()
			start = System.nanoTime()
			for (i in range) kCallable.call(obj, *param)
			end = System.nanoTime()
			end.ofMillis(start).p()
			"java：".pl()
			kCallable.javaField?.let {
				start = System.nanoTime()
				for (i in range) it.get(obj)
				end = System.nanoTime()
				end.ofMillis(start).p()
			}
			kCallable.javaGetter?.let {
				start = System.nanoTime()
				for (i in range) it.invoke(obj, *param)
				end = System.nanoTime()
				end.ofMillis(start).p()
			}
		}
	}
}
