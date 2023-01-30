package my.ktbot.dao.blibili


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VedioStreamUrl(
    @SerialName("accept_description")
    val acceptDescription: List<String>,
    @SerialName("accept_format")
    val acceptFormat: String, // mp4,mp4
    @SerialName("accept_quality")
    val acceptQuality: List<Int>,
    @SerialName("durl")
    val durl: List<Durl>,
    @SerialName("format")
    val format: String, // mp4
    @SerialName("quality")
    val quality: Int, // 16
    @SerialName("support_formats")
    val supportFormats: List<SupportFormat>,
    @SerialName("timelength")
    val timelength: Int, // 204202
    @SerialName("video_codecid")
    val videoCodecid: Int // 7
) {
    @Serializable
    data class Durl(
        @SerialName("ahead")
        val ahead: String,
        @SerialName("backup_url")
        val backupUrl: List<String>,
        @SerialName("length")
        val length: Int, // 204202
        @SerialName("order")
        val order: Int, // 1
        @SerialName("size")
        val size: Int, // 10262148
        @SerialName("url")
        val url: String, // https://xy222x187x225x106xy.mcdn.bilivideo.cn:4483/upgcxcode/85/63/978486385/978486385_nb3-1-16.mp4?e=ig8euxZM2rNcNbRVhwdVhwdlhWdVhwdVhoNvNC8BqJIzNbfqXBvEqxTEto8BTrNvN0GvT90W5JZMkX_YN0MvXg8gNEV4NC8xNEV4N03eN0B5tZlqNxTEto8BTrNvNeZVuJ10Kj_g2UB02J0mN0B5tZlqNCNEto8BTrNvNC7MTX502C8f2jmMQJ6mqF2fka1mqx6gqj0eN0B599M=&uipk=5&nbs=1&deadline=1675000606&gen=playurlv2&os=mcdn&oi=2044822149&trid=000016974f70efa1471fb83ad8ea42c84ebcu&mid=0&platform=pc&upsig=b0bb98925c8a53986e7d1cee97abf5cb&uparams=e,uipk,nbs,deadline,gen,os,oi,trid,mid,platform&mcdnid=17000162&bvc=vod&nettype=0&orderid=0,3&buvid=&build=0&agrr=0&bw=50304&logo=A0010000
        @SerialName("vhead")
        val vhead: String
    )

    @Serializable
    data class SupportFormat(
        @SerialName("display_desc")
        val displayDesc: String, // 1080P
        @SerialName("format")
        val format: String, // mp4
        @SerialName("new_description")
        val newDescription: String, // 1080P 高清
        @SerialName("quality")
        val quality: Int, // 80
    )
}
