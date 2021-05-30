package xyz.pokecord.bot.modules.pokemon.commands

import xyz.pokecord.bot.core.structures.discord.Command
import xyz.pokecord.bot.core.structures.discord.MessageReceivedContext
import xyz.pokecord.bot.core.structures.pokemon.MoveData

class MoveCommand : Command() {
  override val name = "Move"

  @Executor
  suspend fun execute(
    context: MessageReceivedContext,
    @Argument(name = "move", consumeRest = true) moveName: String?
  ) {
    if (moveName == null) {
      return
    }
    val move = MoveData.getByName(moveName)

    if (move == null) {
      return
    }

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
  }
}
