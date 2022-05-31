object Test {
	@JvmStatic
	fun main(args: Array<String>) {
		fun <T : Any?> T.p() = println(this)
		A::class.java.isAssignableFrom(B::class.java).p()
	}

	interface A
	interface B : A
}
