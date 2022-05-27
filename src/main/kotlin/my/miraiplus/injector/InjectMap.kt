package my.miraiplus.injector

import kotlin.reflect.KClass

class InjectMap {
	private val injectorMap = HashMap<Class<out Annotation>, ArrayList<Injector<out Annotation>>>()

	fun <T : Annotation> add(annClass: KClass<T>, injector: Injector<T>) {
		injectorMap.getOrPut(annClass.java) { ArrayList() }.add(injector)
	}

	fun <T : Annotation> add(annClass: Class<T>, injector: Injector<T>) {
		injectorMap.getOrPut(annClass) { ArrayList() }.add(injector)
	}

	inline fun <reified T : Annotation> add(injector: Injector<T>) {
		add(T::class.java, injector)
	}

	fun <T : Annotation> remove(annClass: KClass<T>, injector: Injector<T>): Boolean {
		return injectorMap[annClass.java]?.remove(injector) ?: false
	}

	fun <T : Annotation> remove(annClass: Class<T>, injector: Injector<T>): Boolean {
		return injectorMap[annClass]?.remove(injector) ?: false
	}

	inline fun <reified T : Annotation> remove(injector: Injector<T>): Boolean {
		return remove(T::class.java, injector)
	}

	@Suppress("UNCHECKED_CAST")
	operator fun <T : Annotation> get(annClass: Class<T>): MutableList<Injector<T>>? {
		return injectorMap[annClass] as MutableList<Injector<T>>?
	}

	inline fun <reified T : Annotation> get(): MutableList<Injector<T>>? {
		return get(T::class.java)
	}
}
