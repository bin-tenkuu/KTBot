package my.miraiplus.injector

import net.mamoe.mirai.event.Event
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class InjectMap {
	private val injectorMap = HashMap<Class<out Annotation>, ArrayList<Injector<out Annotation, out Event>>>()

	// region add

	fun <T : Annotation> add(annClass: Class<T>, injector: Injector<T, out Event>): Boolean {
		return injectorMap.getOrPut(annClass) { ArrayList() }.add(injector)
	}

	fun <T : Annotation> add(annClass: KClass<T>, injector: Injector<T, out Event>) = add(annClass.java, injector)

	inline fun <reified T : Annotation> add(injector: Injector<T, out Event>) = add(T::class.java, injector)

	inline operator fun <reified T : Annotation> plus(injector: Injector<T, out Event>): InjectMap {
		add(T::class.java, injector)
		return this
	}

	// endregion

	// region remove

	fun <T : Annotation> remove(annClass: Class<T>, injector: Injector<T, Event>): Boolean {
		return injectorMap[annClass]?.remove(injector) ?: false
	}

	fun <T : Annotation> remove(annClass: KClass<T>, injector: Injector<T, Event>) = remove(annClass.java, injector)

	inline fun <reified T : Annotation> remove(injector: Injector<T, Event>) = remove(T::class.java, injector)

	inline operator fun <reified T : Annotation> minus(injector: Injector<T, Event>): InjectMap {
		remove(T::class.java, injector)
		return this
	}

	inline operator fun <reified T : Annotation> minusAssign(injector: Injector<T, Event>) {
		remove(T::class.java, injector)
	}

	// endregion

	// region get

	@Suppress("UNCHECKED_CAST")
	operator fun <T : Annotation> get(annClass: Class<T>): MutableList<Injector<T, out Event>>? {
		return injectorMap[annClass] as MutableList<Injector<T, out Event>>?
	}

	@JvmName("getByEvent")
	@Suppress("UNCHECKED_CAST")
	operator fun <E : Event> get(event: KClass<E>): List<Injector<out Annotation, out E>> {
		return injectorMap.values.flatten().filter {
			event.isSubclassOf(it.event)
		} as List<Injector<out Annotation, out E>>
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <T : Annotation, E : Event> get(
		annClass: Class<T>, event: KClass<E>,
	): MutableList<Injector<T, out E>>? {
		return this[annClass]?.filter {
			event.isSubclassOf(it.event)
		} as MutableList<Injector<T, out E>>?
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <T : Annotation> get(annClass: KClass<T>) = this[annClass.java]

	inline fun <reified T : Annotation> get() = get(T::class.java)

	// endregion
}
