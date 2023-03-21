package my.ktbot.utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.reflect.*
import my.ktbot.PlugConfig
import my.ktbot.dao.*
import my.ktbot.dao.blibili.BaseApi
import my.ktbot.dao.blibili.LiveData
import my.ktbot.dao.blibili.RoomInit
import my.ktbot.dao.openai.CompletionRequest
import my.ktbot.dao.openai.CompletionResult
import org.jsoup.Jsoup

object KtorUtils {

    fun HttpRequestBuilder.toStatement() = HttpStatement(this, httpClient)

    @JvmStatic
    fun post(urlString: String, body: Any): HttpStatement {
        return HttpRequestBuilder().apply {
            method = HttpMethod.Post
            url.takeFrom(urlString)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(body)
        }.toStatement()
    }

    @JvmStatic
    fun post(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): HttpStatement {
        return HttpRequestBuilder().apply {
            method = HttpMethod.Post
            url.takeFrom(urlString)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            block()
        }.toStatement()
    }

    @JvmStatic
    fun get(urlString: String): HttpStatement {
        return HttpRequestBuilder().apply {
            method = HttpMethod.Get
            url.takeFrom(urlString)
        }.toStatement()
    }

    @JvmStatic
    inline fun get(urlString: String, block: HttpRequestBuilder.() -> Unit): HttpStatement {
        return HttpRequestBuilder().apply {
            method = HttpMethod.Get
            url.takeFrom(urlString)
            block()
        }.toStatement()
    }

    // region 具体请求

    @JvmStatic
    suspend fun pixivPic(url: String): ByteArray {
        return get(url) {
            header(HttpHeaders.Referrer, "https://www.pixiv.net/")
        }.body()
    }

    @JvmStatic
    suspend fun pic(url: String): ByteArray {
        return get(url).body()
    }

    @JvmStatic
    suspend fun pixivCat(pid: Int): PixivCat {
        return post("https://api.pixiv.cat/v1/generate", PixivCatRequest(pid)).body()
    }

    /**
     * lolicon
     * todo:https://lolisuki.cc/#/setu
     * @param request LoliconRequest
     * @return List<Lolicon>
     */
    @JvmStatic
    suspend fun lolicon(request: LoliconRequest): List<Lolicon> {
        return post("https://api.lolicon.app/setu/v2", request).body<LoliconResponse>().data
    }

    /**
     * https://github.com/SocialSisterYi/bilibili-API-collect
     */
    @Suppress("unused")
    @JvmStatic
    suspend fun bilibiliLive(id: Int): List<String> {
        val baseApi = get("https://api.live.bilibili.com/room/v1/Room/room_init") {
            parameter("id", id)
        }.body<BaseApi<RoomInit>>()
        if (baseApi.code != 0) return listOf(baseApi.message)
        if (baseApi.data!!.liveStatus != 1) return listOf("bilibili $id 未开播")
        val roomId = baseApi.data.roomId
        val durl = get("https://api.live.bilibili.com/room/v1/Room/playUrl") {
            parameter("cid", roomId)
            parameter("qn", 10000)
            parameter("platform", "web")
        }.body<BaseApi<LiveData>>().data!!.durl
        return durl.map { it.url }
    }

    /**
     * 祖安
     * @param max [Boolean] 火力全开
     * @return [String]
     */
    @JvmStatic
    @Deprecated("接口无法调用")
    suspend fun zuan(max: Boolean = false): String {
        val url = if (max) "https://zuanbot.com/api.php?lang=zh_cn"
        else "https://zuanbot.com/api.php?lang=zh_cn&level=min"
        return get(url) {
            header(HttpHeaders.Accept, "*/*")
            header(HttpHeaders.Referrer, "https://zuanbot.com/")
        }.body()
    }

    /**
     * 彩虹屁
     * @return String
     */
    @JvmStatic
    suspend fun rainbowFart(): String {
        val text = get("https://api.shadiao.pro/chp") {
            header(HttpHeaders.Origin, "https://chp.shadiao.app")
            header(HttpHeaders.Referrer, "https://chp.shadiao.app/")
        }.body<RainbowFart>().data.text
        println(text)
        return text
    }

    /**
     * 绿茶/渣男语录（海王）
     * @param tea Boolean true-F-绿茶,false-M-渣男
     * @return String
     */
    @JvmStatic
    suspend fun greenTea(tea: Boolean = false): String {
        return get("https://api.lovelive.tools/api/SweetNothings?genderType=" + if (tea) "F" else "M").body()
    }

    /**
     * 60s读懂世界
     * @return ExternalResource
     */

    suspend fun read60sJson(): EveryDay60s? {
        val everyDay60s = get(
            "https://www.zhihu.com/api/v4/columns/c_1261258401923026944/items?limit=1"
        ).body<ZhiHu>()
        val zhihu = everyDay60s.data.firstOrNull() ?: return null
        val content = zhihu.content
        val document = Jsoup.parse(content)
        val src = document.select("figure>img").attr("src")
        val texts = document.select("p").eachText()
        return EveryDay60s(src, texts)
    }

    /**
     * 能不能好好说话？
     * @param text String
     * @return List<String>
     */
    suspend fun nbnhhsh(text: String): List<String> {
        val list = post("https://lab.magiconch.com/api/nbnhhsh/guess", mapOf("text" to text)).body<List<Nbnhhsh>>()
        return list.firstOrNull { it.name == text }?.trans ?: emptyList()
    }

    suspend fun openAiCompletion(text: String): String {
        val completionRequest = CompletionRequest(
            model = "text-davinci-003",
            prompt = "$text<->",
            stop = listOf("<->"),
            maxTokens = 3000
        )
        return try {
            val body = post("https://api.openai.com/v1/completions") {
                setBody(completionRequest)
                header("Authorization", "Bearer " + PlugConfig.openAiToken)
            }.body<CompletionResult>()
            body.choices.firstOrNull()?.text ?: body.error?.message ?: "[WARN]结果为空"
        } catch (e: Exception) {
            "[ERROR]请求失败" + e.message
        }
    }
    // endregion
}
