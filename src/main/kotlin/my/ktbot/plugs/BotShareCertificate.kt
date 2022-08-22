package my.ktbot.plugs

import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.ShareCertificateUtil
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn

object BotShareCertificate {

	@MiraiEventHandle("股票设置")
	@RegexAnn(
		"^[.．。]股票(?<code>(s[hz])(\\d{6}))(?<operater>[ +-])(?<position>\\d+00)(?:\\*(?<price>\\d+.\\d+))?$"
	)
	@NeedAdmin
	@SendAuto
	suspend fun invoke(groups: MatchGroupCollection): String {
		val code = groups["code"]!!.value
		val data = ShareCertificateUtil(listOf(code))[code] ?: return "未知股票代码"
		val isSell = groups["operater"]!!.value == "-"
		val position = groups["position"]!!.value.toInt()
		val price = groups["price"]!!.value.toDouble()
		return "名称:${data.name},代码:${data.code},当前价格:${
			data.currentPrice
		},涨额:${data.raise},涨辐:${data.raiseRange}"
	}
}
