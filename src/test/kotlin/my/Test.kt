package my

import cn.hutool.core.text.UnicodeUtil

object Test : Print {
	@JvmStatic
	fun main(args: Array<String>) {
		val s = "\\u4f60\\u5c31\\u5728\\u6211\\u7684\\u5fc3\\u91cc\\uff0c\\u4e0d\\u8981\\u8d70\\u51fa\\u6765\\u3002\\ud83d\\udc99 \\n\\u2014\\u2014Super junior\\u674e\\u4e1c\\u6d77 \\u674e\\u8d6b\\u5bb0"
		Regex("""\\u\w{4}""").replace(s){
			it.value.substring(2).toInt(16).toChar().toString()
		}.pl()
		UnicodeUtil.toString(s).pl()
	}

}
