package my

import java.time.Duration
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

object TimeTest : Print {
	@JvmStatic
	fun main(vararg args: String) {
		fun Long.ofNanos(start: Long) = Duration.ofNanos(this - start)
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
			@Suppress("UNCHECKED_CAST")
			kCallable as KProperty<Any>
			kCallable.pl()
			kCallable.isAccessible = true
			val param = if (kCallable.parameters.size == 2) arrayOf("abc")
			else arrayOf()

			"kotlin：".p()
			start = System.nanoTime()
			for (i in range) kCallable.call(obj, *param)
			end = System.nanoTime()
			end.ofNanos(start).pl()
			"java：".p()
			kCallable.javaField?.let {
				start = System.nanoTime()
				for (i in range) it.get(obj)
				end = System.nanoTime()
				end.ofNanos(start).pl()
			}
			kCallable.javaGetter?.let {
				start = System.nanoTime()
				for (i in range) it.invoke(obj, *param)
				end = System.nanoTime()
				end.ofNanos(start).pl()
			}
		}
	}
}
