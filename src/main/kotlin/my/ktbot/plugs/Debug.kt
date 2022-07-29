package my.ktbot.plugs

import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.xml.BuildXml
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.SimpleServiceMessage
import net.mamoe.mirai.utils.MiraiExperimentalApi

object Debug {
	/**
	 * 大图使用xml发送
	 */
	@OptIn(MiraiExperimentalApi::class)
	@MiraiEventHandle("test")
	@RegexAnn("^[.．。]test$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@SendAuto
	private fun FriendMessageEvent.run(): Message {
		val serviceID = 5
		val actionData = "mqqapi://card/show_pslcard?src_type=internal&amp;source=sharecard&amp;version=1&amp;uin=2938137849"
		val md5 = "114EB8942E9CF20BC947B4446A56D6C7"
		val toXml = BuildXml("1.0", "UTF-8", true) {
			root("msg").attributes(
				"serviceID" to "$serviceID",
				"templateID" to "12345",
				"brief" to "[图片]",
				"sourceMsgId" to "0",
				"url" to "",
				"flag" to "0",
				"adverSign" to "0",
				"multiMsgFlag" to "0",
			).item("item").attributes(
				"layout" to "0",
				"advertiser_id" to "0",
				"aid" to "0",
			).item("image").attributes(
				"uuid" to "/00-0-$md5",
				"md5" to md5,
				"GroupFiledid" to "0",
				"minWidth" to "100",
				"minHeight" to "100",
				"maxWidth" to "500",
				"maxHeight" to "500",
			)
			root.item("source").attributes(
				"name" to bot.nick,
				"icon" to "",
				"action" to "plugin",
				"actionData" to "AppCmd://OpenContactInfo/?uin=2938137849",
				"url" to actionData,
				"a_actionData" to actionData,
				"i_actionData" to actionData,
				"appid" to "100951776",
			)
		}.toXml()
		println(toXml)
		return SimpleServiceMessage(serviceID, toXml)
	}
}
