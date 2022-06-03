package my.ktbot.utils

import java.time.Duration

/**
 *
 * @author bin
 * @since 2022/1/6
 */
class CacheMap<K, V>(
	/**过期时间,毫秒*/
	private val timeout: Long = Duration.ofMinutes(10).toMillis(),
	initialCapacity: Int = 16,
) {
	private var nextExpirationTime = Long.MAX_VALUE

	private inner class Node(
		val v: V,
		timeout: Long = 0,
	) {
		var time: Long = timeout + System.currentTimeMillis()

		fun isBeOverdue(time: Long = System.currentTimeMillis()) = time >= this.time

		override fun toString(): String = "${if (isBeOverdue()) "timeout" else "waiting"}:${v}"
	}

	private val map = HashMap<K, Node>(initialCapacity)

	val size: Int
		get() {
			expungeExpiredEntries()
			return map.size
		}

	fun clear() {
		map.clear()
		nextExpirationTime = Long.MAX_VALUE
	}

	fun set(key: K, value: V, timeout: Long) {
		expungeExpiredEntries()
		map[key] = Node(value, timeout)
	}

	operator fun set(key: K, value: V) = set(key, value, timeout)

	operator fun get(key: K): V? {
		val node = map[key] ?: return null
		if (node.isBeOverdue()) {
			map.remove(key)
			return null
		}
		return node.v
	}

	fun getOrInit(key: K, block: () -> V): V {
		return get(key) ?: block().also { set(key, it) }
	}

	operator fun contains(key: K): Boolean {
		if ((map[key] ?: return false).isBeOverdue()) {
			map.remove(key)
			return false
		}
		return true
	}

	fun remove(key: K): V? {
		val node = map.remove(key) ?: return null
		if (node.isBeOverdue()) return null
		return node.v
	}

	operator fun minusAssign(key: K) {
		map.remove(key)
	}

	private fun expungeExpiredEntries() {
		val time = System.currentTimeMillis()
		if (nextExpirationTime > time) return
		nextExpirationTime = Long.MAX_VALUE
		val iterator = map.iterator()
		while (iterator.hasNext()) {
			val v = iterator.next().value
			if (v.isBeOverdue(time)) iterator.remove()
			else if (nextExpirationTime > v.time) nextExpirationTime = v.time
		}
	}
}
