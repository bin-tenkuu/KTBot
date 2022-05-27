object Test {
	@JvmStatic
	fun main(args: Array<String>) {
		fun <T : Any> T.p() = println(this)
		// val b = B::class.java
		// val a = A::class.java
		// -b.isAssignableFrom(a)
		// -b.isAssignableFrom(b)
		// -a.isAssignableFrom(b)
		(A::a == B::a).p()
		A::a.hashCode().p()
		A::a.hashCode().p()
		(A::a === A::a).p()
	}

	private open class A {
		open fun a() = Unit
	}

	private open class B : A() {
	}
}
