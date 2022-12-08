package my.ktbot.dao.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author bin
 * @since 2022/12/08
 */
@Serializable
class CompletionRequest(
	/**
	 * The name of the model to use.
	 * Required if specifying a fine tuned model or if using the new v1/completions endpoint.
	 */
	var model: String,

	/**
	 * An optional prompt to complete from
	 */
	var prompt: String? = null,

	/**
	 * The maximum number of tokens to generate.
	 * Requests can use up to 2048 tokens shared between prompt and completion.
	 * (One token is roughly 4 characters for normal English text)
	 */
	@SerialName("max_tokens")
	var maxTokens: Int = 16,

	/**
	 * What sampling temperature to use. Higher values means the model will take more risks.
	 * Try 0.9 for more creative applications, and 0 (argmax sampling) for ones with a well-defined answer.
	 *
	 * We generally recommend using this or [CompletionRequest.topP] but not both.
	 */
	var temperature: Double = 1.0,

	/**
	 * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of
	 * the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are
	 * considered.
	 *
	 * We generally recommend using this or [CompletionRequest.temperature] but not both.
	 */
	@SerialName("top_p")
	var topP: Double = 1.0,

	/**
	 * How many completions to generate for each prompt.
	 *
	 * Because this parameter generates many completions, it can quickly consume your token quota.
	 * Use carefully and ensure that you have reasonable settings for [CompletionRequest.maxTokens] and [CompletionRequest.stop].
	 */
	var n: Int = 1,

	/**
	 * Whether to stream back partial progress.
	 * If set, tokens will be sent as data-only server-sent events as they become available,
	 * with the stream terminated by a data: DONE message.
	 */
	var stream: Boolean = false,

	/**
	 * Include the log probabilities on the logprobs most likely tokens, as well the chosen tokens.
	 * For example, if logprobs is 10, the API will return a list of the 10 most likely tokens.
	 * The API will always return the logprob of the sampled token,
	 * so there may be up to logprobs+1 elements in the response.
	 */
	var logprobs: Int? = null,

	/**
	 * Echo back the prompt in addition to the completion
	 */
	var echo: Boolean = false,

	/**
	 * Up to 4 sequences where the API will stop generating further tokens.
	 * The returned text will not contain the stop sequence.
	 */
	var stop: List<String>? = null,

	/**
	 * Number between 0 and 1 (default 0) that penalizes new tokens based on whether they appear in the text so far.
	 * Increases the model's likelihood to talk about new topics.
	 */
	@SerialName("presence_penalty")
	var presencePenalty: Double = 0.0,

	/**
	 * Number between 0 and 1 (default 0) that penalizes new tokens based on their existing frequency in the text so far.
	 * Decreases the model's likelihood to repeat the same line verbatim.
	 */
	@SerialName("frequency_penalty")
	var frequencyPenalty: Double = 0.0,

	/**
	 * Generates best_of completions server-side and returns the "best"
	 * (the one with the lowest log probability per token).
	 * Results cannot be streamed.
	 *
	 * When used with [CompletionRequest.n], best_of controls the number of candidate completions and n specifies how many to return,
	 * best_of must be greater than n.
	 */
	@SerialName("best_of")
	var bestOf: Int = 1,

	/**
	 * Modify the likelihood of specified tokens appearing in the completion.
	 *
	 * Maps tokens (specified by their token ID in the GPT tokenizer) to an associated bias value from -100 to 100.
	 *
	 * https://beta.openai.com/docs/api-reference/completions/create#completions/create-logit_bias
	 */
	@SerialName("logit_bias")
	var logitBias: Map<String, Int>? = null,

	/**
	 * A unique identifier representing your end-user, which will help OpenAI to monitor and detect abuse.
	 */
	var user: String? = null,
)

@Serializable
class CompletionResult(
	val id: String? = null,
	var `object`: String? = null,
	var created: Long = 0,
	var model: String? = null,
	var choices: List<CompletionChoice> = emptyList(),
	var usage: Usage? = null,
	val error: ErrorResult? = null,
)

@Serializable
class CompletionChoice(
	/**
	 * The generated text. Will include the prompt if [CompletionRequest.echo] is true
	 */
	var text: String = "",

	/**
	 * This index of this completion in the returned list.
	 */
	var index: Int? = null,

	/**
	 * The log probabilities of the chosen tokens and the top [CompletionRequest.logprobs] tokens
	 */
	// var logprobs: LogProbResult? = null,

	/**
	 * The reason why GPT-3 stopped generating, for example "length".
	 */
	var finish_reason: String? = null,
)

@Serializable
class Usage(
	/**
	 * The number of prompt tokens used.
	 */
	var promptTokens: Long = 0,

	/**
	 * The number of completion tokens used.
	 */
	var completionTokens: Long = 0,

	/**
	 * The number of total tokens used
	 */
	var totalTokens: Long = 0,
)

@Serializable
class ErrorResult(
	val message: String,
	val type: String,
	val param: String?,
	val code: String?,
)
