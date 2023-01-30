package my.ktbot.dao.blibili


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/***/
@Serializable
data class VedioInfo(
	/***/
	@SerialName("aid")
	val aid: Int, // 778289400
	/***/
	@SerialName("bvid")
	val bvid: String, // BV1iy4y1d78B
	/***/
	@SerialName("cid")
	val cid: Int, // 978486385
	/***/
	@SerialName("copyright")
	val copyright: Int, // 1
	/***/
	@SerialName("ctime")
	val ctime: Int, // 1674559379
	/***/
	@SerialName("desc")
	val desc: String,
	/***/
	@SerialName("desc_v2")
	val descV2: List<DescV2>,
	/***/
	@SerialName("dimension")
	val dimension: Dimension,
	/***/
	@SerialName("duration")
	val duration: Int, // 205
	/***/
	@SerialName("dynamic")
	val `dynamic`: String, // 暴雪网易分手，炉石停服，你们怎么选？
	/***/
	@SerialName("mission_id")
	val missionId: Int, // 1227312
	/***/
	@SerialName("need_jump_bv")
	val needJumpBv: Boolean, // false
	/***/
	@SerialName("pages")
	val pages: List<Page>,
	/***/
	@SerialName("pic")
	val pic: String, // http://i1.hdslb.com/bfs/archive/e1b584dac088d9d30fc1dd02777ca0207d38257e.jpg
	/***/
	@SerialName("pubdate")
	val pubdate: Int, // 1674560012
	/***/
	@SerialName("rights")
	val rights: Rights,
	/***/
	@SerialName("stat")
	val stat: Stat,
	/***/
	@SerialName("state")
	val state: Int, // 0
	/***/
	@SerialName("teenage_mode")
	val teenageMode: Int, // 0
	/***/
	@SerialName("tid")
	val tid: Int, // 65
	/***/
	@SerialName("title")
	val title: String, // 暴雪关服了才发现只感动了自己，网易是赚了还是赔了？
	/***/
	@SerialName("tname")
	/***/
	val tname: String, // 网络游戏
	/***/
	@SerialName("videos")
	val videos: Int, // 1
) {
	@Serializable
	data class DescV2(
		/***/
		@SerialName("biz_id")
		val bizId: Int, // 0
		/***/
		@SerialName("raw_text")
		val rawText: String, // 表面上看是暴雪游戏停服了，但实际上看其实网易赚麻了，按照网易总收益来比较，暴雪游戏只占网易总收益的百分之个位数，相比于网易代理的其它游戏，营收百分比肯定是要高于暴雪游戏的，但就目前来看暴雪的操作，它已经把中国玩家抬到了一个不得不放弃暴雪游戏的局面。所以针对这次这件事，大家有什么想聊的，都可以在评论区发出自己的看法。PS：过年还给大家更新视频，求求给个三连吧QAQ~
		/***/
		@SerialName("type")
		val type: Int, // 1
	)

	@Serializable
	data class Dimension(
		/***/
		@SerialName("height")
		val height: Int, // 1080
		/***/
		@SerialName("rotate")
		val rotate: Int, // 0
		/***/
		@SerialName("width")
		val width: Int, // 1920
	)

	@Serializable
	data class Page(
		/***/
		@SerialName("cid")
		val cid: Int, // 978486385
		/***/
		@SerialName("dimension")
		val dimension: Dimension,
		/***/
		@SerialName("duration")
		val duration: Int, // 205
		/***/
		@SerialName("first_frame")
		val firstFrame: String, // http://i1.hdslb.com/bfs/storyff/n230124qn1za8687ouc3go3boscsbr6x_firsti.jpg
		/***/
		@SerialName("from")
		val from: String, // vupload
		/***/
		@SerialName("page")
		val page: Int, // 1
		/***/
		@SerialName("part")
		val part: String,
		/***/
		@SerialName("vid")
		val vid: String,
		/***/
		@SerialName("weblink")
		val weblink: String,
	) {
		@Serializable
		data class Dimension(
			/***/
			@SerialName("height")
			val height: Int, // 1080
			/***/
			@SerialName("rotate")
			val rotate: Int, // 0
			/***/
			@SerialName("width")
			val width: Int, // 1920
		)
	}

	@Serializable
	data class Rights(
		/***/
		@SerialName("arc_pay")
		val arcPay: Int, // 0
		/***/
		@SerialName("autoplay")
		val autoplay: Int, // 1
		/***/
		@SerialName("bp")
		val bp: Int, // 0
		/***/
		@SerialName("clean_mode")
		val cleanMode: Int, // 0
		/***/
		@SerialName("download")
		val download: Int, // 1
		/***/
		@SerialName("elec")
		val elec: Int, // 0
		/***/
		@SerialName("free_watch")
		val freeWatch: Int, // 0
		/***/
		@SerialName("hd5")
		val hd5: Int, // 1
		/***/
		@SerialName("is_360")
		val is360: Int, // 0
		/***/
		@SerialName("is_cooperation")
		val isCooperation: Int, // 0
		/***/
		@SerialName("is_stein_gate")
		val isSteinGate: Int, // 0
		/***/
		@SerialName("movie")
		val movie: Int, // 0
		/***/
		@SerialName("no_background")
		val noBackground: Int, // 0
		/***/
		@SerialName("no_reprint")
		val noReprint: Int, // 1
		/***/
		@SerialName("no_share")
		val noShare: Int, // 0
		/***/
		@SerialName("pay")
		val pay: Int, // 0
		/***/
		@SerialName("ugc_pay")
		val ugcPay: Int, // 0
		/***/
		@SerialName("ugc_pay_preview")
		val ugcPayPreview: Int, // 0
	)

	@Serializable
	data class Stat(
		/***/
		@SerialName("aid")
		val aid: Int, // 778289400
		/***/
		@SerialName("argue_msg")
		val argueMsg: String,
		/***/
		@SerialName("coin")
		val coin: Int, // 574
		/***/
		@SerialName("danmaku")
		val danmaku: Int, // 862
		/***/
		@SerialName("dislike")
		val dislike: Int, // 0
		/***/
		@SerialName("evaluation")
		val evaluation: String,
		/***/
		@SerialName("favorite")
		val favorite: Int, // 549
		/***/
		@SerialName("his_rank")
		val hisRank: Int, // 0
		/***/
		@SerialName("like")
		val like: Int, // 14630
		/***/
		@SerialName("now_rank")
		val nowRank: Int, // 0
		/***/
		@SerialName("reply")
		val reply: Int, // 689
		/***/
		@SerialName("share")
		val share: Int, // 132
		/***/
		@SerialName("view")
		val view: Int, // 294202
	)
}
