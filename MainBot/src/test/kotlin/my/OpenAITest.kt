package my

import com.theokanning.openai.OpenAiService
import com.theokanning.openai.completion.CompletionRequest
import com.theokanning.openai.moderation.ModerationRequest
import my.ktbot.PlugConfig
import java.time.Duration

/**
 * @author bin
 * @since 2022/12/07
 */
object OpenAITest {
	private val service = OpenAiService(PlugConfig.openAiToken, Duration.ofSeconds(20))
	private fun listModels() {
		val listModels = service.listModels()
		for (model in listModels) {
			println(model)
		}
	}

	private fun createModeration() {
		val completionRequest = ModerationRequest.builder()
			.input("你是谁")
			.model("text-moderation-latest")
			// .model("text-moderation-playground")
			.build()
		val completionResult = service.createModeration(completionRequest)
		for (choice in completionResult.results) {
			println(choice)
		}
	}

	private fun createCompletion() {
		// for (model in arrayOf(
		// 	"text-davinci-003",
		// 	"text-curie-001",
		// 	"text-babbage-001",
		// 	"text-ada-001",
		// 	"code-davinci-002",
		// 	"code-cushman-001",
		// )) {
		//
		// }
		val completionRequest = CompletionRequest.builder()
			.model("text-davinci-003")
			// .prompt("如果AI可以以假乱真，你想问他一个什么问题？")
			.prompt("如何把TextWatcher转换为kotlin的dsl风格？")
			.maxTokens(3000)
			// .model("text-moderation-playground")
			.build()
		val completionResult = service.createCompletion(completionRequest)
		for (choice in completionResult.choices) {
			println(choice.text)
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		createCompletion()
	}
}
