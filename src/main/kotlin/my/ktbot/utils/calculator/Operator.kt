package my.ktbot.utils.calculator

import my.ktbot.utils.DiceResult
import kotlin.math.pow

/**
 *  @Date:2022/5/1
 *  @author bin
 *  @version 1.0.0
 */


interface Operator : Node {
	val priority: Int
	val op: String
}

interface Operator1 : Operator {
	operator fun invoke(l: IValue): IValue
}

interface Operator2 : Operator {
	operator fun invoke(l: IValue, r: IValue): IValue
}

enum class Operator1Enum(
	override val op: String,
	override val priority: Int,
) : Operator1 {
	UnaryPlus("+", 15) {
		override fun invoke(l: IValue): IValue = l
	},
	UnaryMinus("-", 15) {
		override fun invoke(l: IValue): IValue = NumberNode(-l.v.toDouble())
	},
	Inv("~", 15) {
		override fun invoke(l: IValue): IValue = NumberNode(l.v.toLong().inv())
	},
}

enum class Operator2Enum(
	override val op: String,
	override val priority: Int,
) : Operator2 {
	Dice("d", 20) {
		override fun invoke(l: IValue, r: IValue): IValue = DiceNode(DiceResult(l.v.toInt(), r.v.toInt()).dice())
	},
	Pow("**", 16) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toDouble().pow(r.v.toDouble()))
	},
	Times("*", 15) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toDouble() * r.v.toDouble())
	},
	Div("/", 15) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toDouble() / r.v.toDouble())
	},
	Rem("%", 15) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toDouble() % r.v.toDouble())
	},
	Plus("+", 14) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toDouble() + r.v.toDouble())
	},
	Minus("-", 14) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toDouble() - r.v.toDouble())
	},
	Shl("<<", 11) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toLong() shl r.v.toInt())
	},
	Shr(">>", 11) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toLong() shr r.v.toInt())
	},
	Ushr(">>>", 11) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toLong() ushr r.v.toInt())
	},
	And("&", 8) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toLong() and r.v.toLong())
	},
	Xor("^", 7) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toLong() xor r.v.toLong())
	},
	Or("|", 6) {
		override fun invoke(l: IValue, r: IValue): IValue = NumberNode(l.v.toLong() or r.v.toLong())
	},
}
