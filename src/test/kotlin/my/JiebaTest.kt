package my

import com.huaban.analysis.jieba.JiebaSegmenter

object JiebaTest : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		val text = "前任拉甘送苏宁首败落后恒大6分争冠难了"
		val segmenter = JiebaSegmenter()
		println(segmenter.sentenceProcess(text))
	}
}
