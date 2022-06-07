package my

object Test : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		IntArray::class.java.pl()
		Array<Array<Int>>::class.java.pl()
	}

}
