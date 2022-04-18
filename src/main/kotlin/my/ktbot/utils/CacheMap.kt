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
		fun isBeOverdue(time: Long = System.currentTimeMillis()): Boolean = time >= this.time
	}

	private val map = HashMap<K, Node>(initialCapacity)

	val size: Int
		get() {
			expungeExpiredEntries()
			return map.size
		}

	fun clear() {
		expungeExpiredEntries()
		map.clear()
	}

	fun set(key: K, value: V, timeout: Long) {
		expungeExpiredEntries()
		map[key] = Node(value, timeout)
	}

	operator fun set(key: K, value: V) = set(key, value, timeout)

	operator fun get(key: K): V? {
		val node = map[key] ?: return null
		if (node.isBeOverdue()) {
			map -= key
			return null
		}
		return node.v
	}

	fun getOrInit(key: K, block: () -> V): V {
		return get(key) ?: block().also { set(key, it) }
	}

	operator fun contains(key: K): Boolean {
		if ((map[key] ?: return false).isBeOverdue()) {
			map -= key
			return false
		}
		return true
	}

	operator fun minusAssign(key: K) {
		remove(key)
	}

	fun remove(key: K) {
		map.remove(key)
	}

	private fun expungeExpiredEntries() {
		val time = System.currentTimeMillis()
		if (nextExpirationTime > time) {
			return
		}
		nextExpirationTime = Long.MAX_VALUE
		val iterator = map.iterator()
		while (iterator.hasNext()) {
			val (_, v) = iterator.next()
			if (v.isBeOverdue(time)) {
				iterator.remove()
			}
			else if (nextExpirationTime > v.time) {
				nextExpirationTime = v.time
			}
		}
	}
}