package my.ktbot.plugin.dao

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * @author bin
 * @since 1.0
 * @date 2022/1/17
 */
@Serializable
data class PixivCatRequest(
	val p: Int,
)

@Serializable
data class PixivCat(
	/**作者*/
	val artist: Artist,
	/**pid*/
	val id: Int = 0, // 92976132
	/**是否多个*/
	val multiple: Boolean, // false
	/***/
	@SerialName("original_url")
	val originalUrl: String?, // https://i.pximg.net/img-original/img/2021/09/24/11/05/26/92976132_ugoira0.jpg
	@SerialName("original_urls")
	val originalUrls: List<String>?,
	/***/
	@SerialName("original_url_proxy")
	val originalUrlProxy: String?, // https://i.pixiv.cat/img-original/img/2021/09/24/11/05/26/92976132_ugoira0.jpg
	@SerialName("original_urls_proxy")
	val originalUrlsProxy: List<String>?,
//	val multiple: Boolean, // true
	val thumbnails: List<String> = emptyList(),
	/**是否成功*/
	val success: Boolean, // true
	/**标题*/
	val title: String = "", // [GIF] JackO challenge(?)
) {
	@Serializable
	data class Artist(
		/**uid*/
		val id: Int = 0, // 6241630
		/**用户名称*/
		val name: String = "", // ranken
	)
}