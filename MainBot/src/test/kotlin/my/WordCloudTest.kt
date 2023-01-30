package my

import com.huaban.analysis.jieba.JiebaSegmenter
import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.CircleBackground
import com.kennycason.kumo.bg.PixelBoundaryBackground
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.LinearFontScalar
import com.kennycason.kumo.image.AngleGenerator
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizer.api.WordTokenizer
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import my.ktbot.PluginMain
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import java.awt.Color
import java.awt.Dimension
import java.io.ByteArrayOutputStream
import java.io.File

object WordCloudTest {
	@JvmStatic
	fun main(text: Array<String>) {
		val frequencyAnalyzer = FrequencyAnalyzer()
		frequencyAnalyzer.setWordFrequenciesToReturn(300)
		frequencyAnalyzer.setMinWordLength(2)
		frequencyAnalyzer.setWordTokenizer(WordCloudConfig.tokenizer.instance)
		val wordFrequencies = frequencyAnalyzer.load(text.toList())
		val dimension = Dimension(WordCloudConfig.width, WordCloudConfig.height)
		val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT)
		wordCloud.setPadding(2)
		wordCloud.setAngleGenerator(AngleGenerator(0))
		wordCloud.setKumoFont(
			if (WordCloudConfig.fontPath == "default")
				KumoFont(PluginMain.getResourceAsStream("萝莉体.ttf"))
			else
				KumoFont(File(WordCloudConfig.fontPath))
		)
		val colors = WordCloudConfig.colorList
		wordCloud.setBackground(
			when (WordCloudConfig.backgroundMode) {
				BackGround.CIRCLE -> CircleBackground(((WordCloudConfig.height + WordCloudConfig.width) / 4))
				BackGround.IMAGE -> PixelBoundaryBackground(File(WordCloudConfig.imagePath!!))
			}
		)
		wordCloud.setBackgroundColor(Color(0xFFFFFF))
		wordCloud.setColorPalette(
			ColorPalette(colors.map { Color(it.toIntOrNull(16) ?: return@map null) })
		)
		wordCloud.setFontScalar(LinearFontScalar(WordCloudConfig.minFontSize, WordCloudConfig.maxFontSize))
		wordCloud.build(wordFrequencies)
		val stream = ByteArrayOutputStream()
		wordCloud.writeToStreamAsPNG(stream)
		stream.toByteArray()
	}

	enum class BackGround {
		CIRCLE,
		IMAGE
	}

	enum class Tokenizers(val instance: WordTokenizer) {
		JIEBA(JieBaTokenizer()),
		KUMO(ChineseWordTokenizer())
	}

	class JieBaTokenizer : WordTokenizer {
		override fun tokenize(sentence: String?): MutableList<String> {
			val segmenter = JiebaSegmenter()
			return segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).map {
				it.word.trim()
			}.toMutableList()
		}
	}

	object WordCloudConfig : AutoSavePluginConfig("config") {
		@ValueDescription("设置背景宽度")
		val width: Int by value(1000)

		@ValueDescription("设置背景高度")
		val height: Int by value(1000)

		@ValueDescription("设置最小文字大小")
		val minFontSize: Int by value(10)

		@ValueDescription("设置最大文字大小")
		val maxFontSize: Int by value(40)

		@ValueDescription("将自动过滤匹配到的内容")
		val regexs: List<String> by value(
			listOf(
				"[1-9][0-9]{4,14}",
				"""\[.*\]"""
			)
		)

		@ValueDescription("设置词云的背景模式,可选 CIRCLE,IMAGE两种")
		val backgroundMode: BackGround by value(BackGround.CIRCLE)

		@ValueDescription("若背景模式为IMAGE,则需要此项来指定背景图片,图片中填充文字之外的部分需用透明色表示")
		val imagePath: String? by value()

		@ValueDescription("给/WordCloud设置别名")
		val alias: MutableList<String> by value(mutableListOf("词云"))

		@ValueDescription("选择分词器,可选 JIEBA,KUMO 两种")
		val tokenizer: Tokenizers by value(Tokenizers.KUMO)

		@ValueDescription("设置词云的字体")
		val fontPath: String by value("default")

		@ValueDescription("设置词云文字可选的颜色,用16进制表示")
		val colorList: MutableList<String> by value(
			mutableListOf(
				"0000FF",
				"40D3F1",
				"40C5F1",
				"40AAF1",
				"408DF1",
				"4055F1"
			)
		)
	}
}
