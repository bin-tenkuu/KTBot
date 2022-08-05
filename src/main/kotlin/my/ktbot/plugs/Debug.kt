package my.ktbot.plugs

import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.xml.XmlEntity
import my.miraiplus.annotation.MiraiEventHandle
import my.miraiplus.annotation.RegexAnn
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi

object Debug {
	@OptIn(MiraiExperimentalApi::class)
	@MiraiEventHandle("test")
	@RegexAnn("^[.．。]test$", RegexOption.IGNORE_CASE)
	@NeedAdmin
	@SendAuto
	private fun MessageEvent.run(): Message {
		val image = message.firstIsInstanceOrNull<Image>() ?: return PlainText("图片")
		val toXml = buildXml(image, bot.nick).toXml()
		return SimpleServiceMessage(5, toXml)
	}

	@MiraiEventHandle("all")
	@NeedAdmin
	private fun FriendMessageEvent.test() {
		message
	}

	private fun String.imageIdToMd5(): String {
		return replace("-", "").substring(1, 33)
	}

	private fun buildXml(image: Image, name: String): XmlEntity {
		val md5 = image.imageId.imageIdToMd5()
		val serviceID = 5
		val actionData = "mqqapi://card/show_pslcard?src_type=internal&amp;source=sharecard&amp;version=1&amp;uin=2938137849"
		return XmlEntity("1.0", "UTF-8", true) {
			root("msg").attributes(
				"serviceID" to "$serviceID",
				"templateID" to "12345",
				"brief" to "[图片]",
			).item("item").attributes(
				"layout" to "0"
			).item("image").attributes(
				"uuid" to image.imageId,
				"md5" to md5,
				"GroupFiledid" to "0",
				"filesize" to image.size.toString(),
				"minWidth" to "100",
				"minHeight" to "100",
				"maxWidth" to image.width.coerceAtLeast(500).toString(),
				"maxHeight" to image.height.coerceAtLeast(500).toString(),
			)
			root.item("source").attributes(
				"name" to name,
				"icon" to "",
				"appid" to "100951776",
				"action" to "plugin",
				"actionData" to "AppCmd://OpenContactInfo/?uin=2938137849",
				"i_actionData" to actionData,
				"a_actionData" to actionData,
				"url" to actionData,
			)
		}
	}
}
