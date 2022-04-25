package org.example.mirai.plugin

import my.ktbot.utils.ReplaceNode

fun main() {
	val root = ReplaceNode() + mapOf(
		"吗" to "",
		"是" to "不是",
		"不是" to "是",
		"是不是" to "肯定是",
	)
	for (s in listOf(
		"是不是吗",
		"\\是不是吗",
		"是\\不是吗",
		"\\是\\不是吗",
		"是不\\是吗",
		"\\是不\\是吗",
		"是\\不\\是吗",
		"\\是\\不\\是吗",
		"是不是\\吗",
		"\\是不是\\吗",
		"是\\不是\\吗",
		"\\是\\不是\\吗",
		"是不\\是\\吗",
		"\\是不\\是\\吗",
		"是\\不\\是\\吗",
		"\\是\\不\\是\\吗"
	)) {
		print(s)
		print(" -> ")
		println(root.replace(s))
	}
}

