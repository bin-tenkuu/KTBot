

object Test {
	@JvmStatic
	fun main(args: Array<String>) {
		fun <T : Any?> T.p() = println(this)
		val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
		val it = list.listIterator()
		while (it.hasNext()){
			it.next().p()
		}
		while (it.hasPrevious()){
			it.previous().p()
		}
	}

}
