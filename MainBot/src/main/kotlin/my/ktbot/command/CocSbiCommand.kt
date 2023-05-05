package my.ktbot.command

import my.ktbot.PluginMain
import my.ktbot.PluginPerm
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.descriptor.CommandArgumentContext
import net.mamoe.mirai.console.command.descriptor.EmptyCommandArgumentContext
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * @author bin
 * @since 2023/05/05
 */
@OptIn(ConsoleExperimentalApi::class)
@Suppress("unused")
object CocSbiCommand {
    private val owner = PluginMain
    private val parentPermission = PluginPerm.coc
    private val overrideContext: CommandArgumentContext = EmptyCommandArgumentContext

    val all: Array<out Command> by lazy {
        this::class.nestedClasses.mapNotNull {
            it.objectInstance as? Command
        }.toTypedArray()
    }

    private open class SubCommand(
            @ResolveContext(ResolveContext.Kind.COMMAND_NAME) primaryName: String,
            description: String = "no description available",
            @ResolveContext(ResolveContext.Kind.COMMAND_NAME) vararg secondaryNames: String,
    ) : SimpleCommand(
            owner = owner,
            parentPermission = parentPermission,
            primaryName = primaryName,
            secondaryNames = secondaryNames,
            description = description,
            overrideContext = overrideContext
    )

}
