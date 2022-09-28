package my.ktbot.dao.blibili


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class LiveData(
	/**可选画质数参数:qn画质代码*/
	@SerialName("accept_quality")
	val acceptQuality: List<String>,
	/**当前画质代码quality*/
	@SerialName("current_qn")
	val currentQn: Int, // 10000
	/**当前画质代码qn*/
	@SerialName("current_quality")
	val currentQuality: Int, // 4
	/**直播流url组*/
	@SerialName("durl")
	val durl: List<Durl>,
	/**可选画质参数quality*/
	@SerialName("quality_description")
	val qualityDescription: List<QualityDescription>,
) {
	@Serializable
	data class Durl(
		/**0*/
		@SerialName("length")
		val length: Int, // 0
		/**服务器线路序号*/
		@SerialName("order")
		val order: Int, // 1
		/**0*/
		@SerialName("p2p_type")
		val p2pType: Int, // 0
		/**0*/
		@SerialName("stream_type")
		val streamType: Int, // 0
		/**直播流url,flv或m3u8格式,注：带有转义*/
		@SerialName("url")
		val url: String,
	)

	@Serializable
	data class QualityDescription(
		/**该代码对应的画质名称*/
		val desc: String, // 原画
		/**画质代码*/
		val qn: Int, // 10000
	)
}
