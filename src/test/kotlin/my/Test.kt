package my

object Test : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		// Generate a tree with a depth of 100_000
		val deepTree = generateSequence(Tree()) { prev ->
			Tree(prev, Tree())
		}.take(100).last()

		println(calculateDepth(deepTree)) // 100
	}

	class Tree(val left: Tree? = null, val right: Tree? = null)

	private val calculateDepth = DeepRecursiveFunction<Tree?, Int> { t ->
		if (t == null) 0
		else callRecursive(t.left) + callRecursive(t.right) + 1
	}
}
