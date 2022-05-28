object Test {
	@JvmStatic
	fun main(args: Array<String>) {
		fun <T : Any?> T.p() = println(this)
		Regex("").find(" afdas").p()
	}

}
