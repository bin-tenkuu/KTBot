package my.ktbot.plugin.dao

import kotlinx.serialization.Serializable

@Serializable
data class LoliconResponse(
	/**错误信息*/
	val data: List<Lolicon> = listOf(),
	/**色图数组*/
	val error: String = "",
)

@Serializable
data class Lolicon(
	/**作者名（入库时，并过滤掉 @ 及其后内容）*/
	val author: String, // 琥珀むろ
	/**图片扩展名*/
	val ext: String, // png
	/**原图高度 px*/
	val height: Int, // 2200
	/**作品所在页*/
	val p: Int, // 0
	/**作品 pid*/
	val pid: Int, // 72683172
	/**是否 R18（在库中的分类，不等同于作品本身的 R18 标识）*/
	val r18: Boolean, // false
	/**作品标签，包含标签的中文翻译（有的话）*/
	val tags: List<String>,
	/**作品标题*/
	val title: String, // 彼シャツ
	/**作者 uid*/
	val uid: Int, // 861222
	/**作品上传日期；时间戳，单位为毫秒*/
	val uploadDate: Long, // 1547563565000
	/**包含了所有指定size的图片地址*/
	val urls: Map<String, String>,
	/**原图宽度 px*/
	val width: Int, // 1600
)

@Serializable
data class LoliconRequest(
	/**0为非 R18，1为 R18，2为混合（在库中的分类，不等同于作品本身的 R18 标识）*/
	val r18: Int = 0,
	/**一次返回的结果数量，范围为1到100；在指定关键字或标签的情况下，结果数量可能会不足指定的数量*/
	val num: Int = 1,
	/**返回指定uid作者的作品，最多20个*/
	val uid: List<Int>? = null,
	/**返回从标题、作者、标签中按指定关键字模糊匹配的结果，大小写不敏感，性能和准度较差且功能单一，建议使用tag代替*/
	val keyword: String = "",
	/**返回匹配指定标签的作品，详见下文*/
	val tag: List<String>? = null,
	/**返回指定图片规格的地址，详见下文*/
	val size: List<String> = listOf("original"),
	/**设置图片地址所使用的在线反代服务，详见下文*/
	val proxy: String = "i.pixiv.cat",
	/**返回在这个时间及以后上传的作品；时间戳，单位为毫秒*/
	val dateAfter: Int? = null,
	/**返回在这个时间及以前上传的作品；时间戳，单位为毫秒*/
	val dateBefore: Int? = null,
	/**设置为任意真值以禁用对某些缩写keyword和tag的自动转换，详见下文*/
	val dsc: Boolean = false,
)