package xyz.pokecord.bot.modules.pokepedia.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.MoveData

class MoveCommand : Command() {
  override val name = "Move"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "move", consumeRest = true) moveName: String?
  ) {
    try {
      if (moveName == null) {
        return
      }
      val move = MoveData.getByName(moveName) ?: return

      context.reply(
        context.embedTemplates.normal(
          """
          **${context.translate("misc.texts.generation")}**: ${move.romanGenerationId} (${move.generationId})
          **${context.translate("misc.texts.type")}**: ${context.translator.type(move.type)}
          **${context.translate("misc.texts.power")}**: ${move.power}
          **${context.translate("misc.texts.pp")}**: ${move.pp}
          **${context.translate("misc.texts.accuracy")}**: ${move.accuracy}
          **${context.translate("misc.texts.priority")}**: ${move.priority}
          **${context.translate("misc.texts.ailmentChance")}**: ${move.meta.ailmentChance}
          **${context.translate("misc.texts.criticalRate")}**: ${move.meta.criticalRate}
          **${context.translate("misc.texts.damageClass")}**: ${context.translator.damageClass(move.damageClassId)}
        """.trimIndent(),
          "#${move.id} | ${move.name} | ${context.translate("misc.texts.moveInfo")}"
        ).build()
      ).queue()
    } catch (e: Throwable) {
      // catch "missing meta moves" stuff
    }
  }
}
