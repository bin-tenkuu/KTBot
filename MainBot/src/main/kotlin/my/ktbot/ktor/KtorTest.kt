package my.ktbot.ktor

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.dataconversion.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.*
import io.ktor.util.debug.*
import io.ktor.util.debug.plugins.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import my.ktbot.PluginMain

/**
 * @author bin
 * @date 2022/09/08
 */
object KtorTest {
	fun run(block: Application.() -> Unit): NettyApplicationEngine {
		return embeddedServer(Netty, port = 85, host = "0.0.0.0", configure = {
		}) {
			globalInit()
			block()
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		val app = run {
			routingArticles()
			this.routingDate()
		}
		app.start(true)
		print("???")
	}

	private fun Application.globalInit() {
		install(CORS) {
			anyHost()
		}
		install(Compression)
		install(Routing)
		install(Resources)
		install(StatusPages) {
			exception<Throwable> { call, cause ->
				call.respondText("500: ${cause.message}", status = HttpStatusCode.InternalServerError)
			}
		}
		install(ContentNegotiation) {
			// register(ContentType.Application.Json, KotlinxSerializationConverter(Json {}))
		}
		install(DataConversion)
		// install(ShutDownUrl.ApplicationCallPlugin) {
		// 	shutDownUrl = "/shutdown"
		// 	exitCodeSupplier = { 0 }
		// }
		install(ShutDown)
	}

	@Suppress("unused")
	@Serializable
	@Resource("/articles")
	class Articles(val sort: String? = "new") {
		@Serializable
		@Resource("new")
		class New(val parent: Articles = Articles())

		@Serializable
		@Resource("{id}")
		class Id(val parent: Articles = Articles(), val id: Long) {
			@Serializable
			@Resource("edit")
			class Edit(val parent: Id)
		}
	}

	fun Application.routingArticles() {
		routing {
			get<Articles> { article ->
				// Get all articles ...
				call.respondText("List of articles sorted starting from ${article.sort}")
			}
			get<Articles.New> {
				// Show a page with fields for creating a new article ...
				call.respondText("Create a new article")
			}
			post<Articles> {
				// Save an article ...
				call.respondText("An article is saved", status = HttpStatusCode.Created)
			}
			get<Articles.Id> { article ->
				// Show an article with id ${article.id} ...
				call.respondText("An article with id ${article.id}", status = HttpStatusCode.OK)
			}
			get<Articles.Id.Edit> { article ->
				// Show a page with fields for editing an article ...
				call.respondText("Edit an article with id ${article.parent.id}", status = HttpStatusCode.OK)
			}
			put<Articles.Id> { article ->
				// Update an article ...
				call.respondText("An article with id ${article.id} updated", status = HttpStatusCode.OK)
			}
			delete<Articles.Id> { article ->
				// Delete an article ...
				call.respondText("An article with id ${article.id} deleted", status = HttpStatusCode.OK)
			}
		}
	}

	fun Application.routingDate() {
		routing {
			get("/") {
				call.respondText("Hello World")
			}
		}
	}

	object ShutDown : BaseApplicationPlugin<Application, Unit, Unit> {
		override val key: AttributeKey<Unit> = AttributeKey("shutdown.url")

		override fun install(pipeline: Application, configure: Unit.() -> Unit) {
			pipeline.intercept(ApplicationCallPipeline.Plugins) {
				initContextInDebugMode {

				}
				useContextElementInDebugMode(PluginsTrace) { trace ->
					trace.eventOrder.add(
						PluginTraceElement(key.name, "onCall", PluginTraceElement.PluginEvent.STARTED)
					)
				}
				addToContextInDebugMode(key.name) {
					if (call.request.uri == "/shutdown") {
						call.application.log.warn("Shutdown URL was called: server is going down")
						val application = call.application
						val environment = application.environment

						val latch = CompletableDeferred<Nothing>()
						call.application.launch {
							latch.join()

							environment.monitor.raise(ApplicationStopPreparing, environment)
							if (environment is ApplicationEngineEnvironment) {
								environment.stop()
							}
							else {
								application.dispose()
							}

							PluginMain.showdown()
						}
						try {
							call.respondText(Math.random().toString(), status = HttpStatusCode.Gone)
						}
						finally {
							latch.cancel()
						}
					}
				}
				useContextElementInDebugMode(PluginsTrace) { trace ->
					trace.eventOrder.add(
						PluginTraceElement(key.name, "onCall", PluginTraceElement.PluginEvent.FINISHED)
					)
				}
			}
		}
	}
}
