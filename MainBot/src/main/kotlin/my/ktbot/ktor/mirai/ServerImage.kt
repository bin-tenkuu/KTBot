package my.ktbot.ktor.mirai

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.MiraiExperimentalApi

class ServerImage : Image {
    override val height: Int get() = 0
    override val imageId: String get() = ""
    override val imageType: ImageType get() = ImageType.UNKNOWN
    override val size: Long get() = 0
    override val width: Int get() = 0

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
    }

    override fun contentToString(): String {
        return ""
    }

    override fun toString(): String {
        return ""
    }

}
