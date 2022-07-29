package my.ktbot.plugs

import my.ktbot.annotation.NeedAdmin
import my.ktbot.annotation.SendAuto
import my.ktbot.utils.xml.BuildXml
import my.miraiplus.annotation.MiraiEventHandle
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.SimpleServiceMessage
import net.mamoe.mirai.message.data.emptyMessageChain
import net.mamoe.mirai.utils.MiraiExperimentalApi

object Debug {
	/**
	 * 大图使用xml发送
	 * @receiver FriendMessageEvent
	 * @return Message
	 */
	@OptIn(MiraiExperimentalApi::class)
	@MiraiEventHandle("debug")
	@NeedAdmin
	@SendAuto
	private fun FriendMessageEvent.run(): Message {
		if (message.contentToString() != "test") {
			return emptyMessageChain()
		}
		val toXml = BuildXml {
			config {
				encoding = "UTF-8"
				standalone = true
			}
			root("msg") {
				attributes["serviceID"] = "5"
				attributes["templateID"] = "12345"
				attributes["brief"] = "[图片]"
				attributes["sourceMsgId"] = "0"
				attributes["url"] = ""
				attributes["flag"] = "0"
				attributes["adverSign"] = "0"
				attributes["multiMsgFlag"] = "0"
				item("item") {
					attributes["layout"] = "0"
					attributes["advertiser_id"] = "0"
					attributes["aid"] = "0"
					item("image") {
						attributes["uuid"] = "/2938137849-2440321223-114EB8942E9CF20BC947B4446A56D6C7"
						attributes["md5"] = "114EB8942E9CF20BC947B4446A56D6C7"
						attributes["GroupFiledid"] = "0"
						// attributes["filesize"] = "30028"
						// attributes["local_path"] = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/MobileQQ/chatpic/chatimg/8fb/Cache_-2fbc38e98d5138fb"
						attributes["minWidth"] = "100"
						attributes["minHeight"] = "100"
						attributes["maxWidth"] = "500"
						attributes["maxHeight"] = "500"
					}
				}
				item("source") {
					attributes["name"] = bot.nick
					attributes["icon"] = ""
					attributes["action"] = ""
					attributes["appid"] = "100951776"
				}
			}
		}.toXml()
		println(toXml)
		return SimpleServiceMessage(5, toXml)
	}
}
