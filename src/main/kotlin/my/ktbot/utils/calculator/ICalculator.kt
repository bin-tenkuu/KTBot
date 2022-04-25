package my.ktbot.utils.calculator

import my.ktbot.utils.DiceResult


internal open class NumberNode(override val v: Number) : Node, IValue {
	override fun toString(): String = v.toString()
}

internal class DataNode(v: Number, val list: List<Any>) : NumberNode(v)
internal class DiceNode(val d: DiceResult) : NumberNode(d.sum)

internal class StringNode(val s: List<String>) : Node
internal class Operator2(
	override val op: String,
	override val priority: Int,
	val v: IOperator2,
) : Operator

internal class Operator1(
	override val op: String,
	override val priority: Int,
	val v: IOperator1,
) : Operator

interface Node

interface Operator : Node {
	val priority: Int
	val op: String
}

interface IValue {
	val v: Number
}
typealias IOperator1 = (IValue) -> IValue
typealias IOperator2 = (IValue, IValue) -> IValue
