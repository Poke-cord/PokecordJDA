package xyz.pokecord.bot.modules.pokemon.commands

import net.dv8tion.jda.api.entities.User
import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.structures.discord.SlashCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.core.structures.pokemon.ItemData
import xyz.pokecord.bot.core.structures.pokemon.Stat
import xyz.pokecord.bot.utils.PokemonResolvable
import xyz.pokecord.bot.utils.extensions.asTrainerId
import java.time.Instant

class InfoCommand : Command() {
  override val name = "Info"

  override var aliases = arrayOf("i")

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(name = "pokemon", optional = true) pokemonResolvable: PokemonResolvable?,
    @Argument(optional = true) user: User?,
  ) {
    if (!context.hasStarted(true)) return
    val targetUser = user ?: context.author
    val checkingSelf = user == null

    val userData = if (checkingSelf) context.getUserData() else module.bot.database.userRepository.getUser(targetUser)
    // TODO: moderator check
    if (userData.progressPrivate && !checkingSelf) {
      context.reply(context.embedTemplates.progressPrivate(targetUser).build()).queue()
      return
    }

    if (context is SlashCommandContext) {
      context.deferReply().queue()
    }

    val pokemon = context.resolvePokemon(targetUser, userData, pokemonResolvable)

    if (pokemon == null) {
      context.reply(context.embedTemplates.error(context.translate("misc.errors.pokemonNotFound")).build())
        .queue()
      return
    }

    val infoSection = """
        **${context.translate("misc.texts.xp")}**: ${if (pokemon.level >= 100) "Max" else "${pokemon.xp}/${pokemon.requiredXpToLevelUp()}"}
        **${context.translate("misc.texts.gender")}**: ${context.translator.gender(pokemon)}
        **${context.translate("misc.texts.nature")}**: ${context.translator.nature(pokemon.nature)}
        ${if (pokemon.heldItemId != 0) "**${context.translate("misc.texts.heldItem")}**: ${(ItemData.getById(pokemon.heldItemId)?.name ?: pokemon.heldItemId)}" else ""}
        """.trimIndent()

    val statSection = """
        **${context.translator.stat(Stat.hp)}**: ${pokemon.stats.hp} - ${pokemon.ivs.hp}/31
        **${context.translator.stat(Stat.attack)}**: ${pokemon.stats.attack} - ${pokemon.ivs.attack}/31
        **${context.translator.stat(Stat.defense)}**: ${pokemon.stats.defense} - ${pokemon.ivs.defense}/31
        **${context.translator.stat(Stat.specialAttack)}**: ${pokemon.stats.specialAttack} - ${pokemon.ivs.specialAttack}/31
        **${context.translator.stat(Stat.specialDefense)}**: ${pokemon.stats.specialDefense} - ${pokemon.ivs.specialDefense}/31
        **${context.translator.stat(Stat.speed)}**: ${pokemon.stats.speed} - ${pokemon.ivs.speed}/31
        **${context.translate("misc.texts.totalIv")}**: ${pokemon.ivPercentage}
        """.trimIndent()

    val trainerSection = """
      **${context.translate("misc.texts.trainerID")}**: ${pokemon.ownerId.asTrainerId}
      ${
      if (pokemon.trainerId != null) "**${
        context.translate(
          "misc.texts.originalTrainerID"
        )
      }**: ${pokemon.trainerId.asTrainerId}" else ""
    }
      """.trimIndent()

    context.reply(
      context.embedTemplates.normal(
        infoSection + "\n" + statSection + "\n\n" + trainerSection,
        context.translate(
          "modules.pokemon.commands.info.embed.title",
          mapOf(
            "level" to pokemon.level.toString(),
            "pokemon" to context.translator.pokemonDisplayName(pokemon),
            "speciesId" to pokemon.data.formattedSpeciesId
          )
        )
      )
        .setImage(pokemon.imageUrl)
        .setFooter(
          context.translate(
            "modules.pokemon.commands.info.embed.footer",
            mapOf(
              "index" to "${pokemon.index + 1}",
              "total" to userData.pokemonCount.toString()
            )
          )
        )
        .setTimestamp(Instant.ofEpochMilli(pokemon.timestamp))
        .setColor(pokemon.data.species.color.colorCode)
        .build()
    ).queue()

//    val stats = listOf(
//      StatInfo(Stat.hp, pokemon.ivs.hp, pokemon.stats.hp),
//      StatInfo(Stat.attack, pokemon.ivs.attack, pokemon.stats.attack),
//      StatInfo(Stat.defense, pokemon.ivs.defense, pokemon.stats.defense),
//      StatInfo(Stat.specialAttack, pokemon.ivs.specialAttack, pokemon.stats.specialAttack),
//      StatInfo(Stat.specialDefense, pokemon.ivs.specialDefense, pokemon.stats.specialDefense),
//      StatInfo(Stat.speed, pokemon.ivs.speed, pokemon.stats.speed),
//    )

//    val statNames = stats.associate {
//      it.stat to context.translator.stat(it.stat)
//    }

//    val longestNameLength = statNames.values.maxOf { it.length }

//    context.reply(
//      context.embedTemplates.normal(
//        """
//        **${context.translate("misc.texts.trainerID")}** ${pokemon.ownerId.asTrainerId}${
//          if (pokemon.trainerId != null) "\n**${
//            context.translate(
//              "misc.texts.originalTrainerID"
//            )
//          }** ${pokemon.trainerId.asTrainerId}" else ""
//        }
//        **${context.translate("misc.texts.gender")}** ${context.translator.gender(pokemon)}
//        **${context.translate("misc.texts.nature")}** ${context.translator.nature(pokemon.nature)}
//        **${context.translate("misc.texts.xp")}** ${if (pokemon.level >= 100) "Max" else "${pokemon.xp}/${pokemon.requiredXpToLevelUp()}"}
//
//        **${context.translate("misc.texts.ivSpread")}
//        **
//        """.trimIndent() +
//            stats.joinToString("\n") {
//              "`${statNames[it.stat]!!.padEnd(longestNameLength, ' ')} | ${
//                it.value.toString().padStart(3, ' ')
//              } | ${it.iv.toString().padStart(2, '0')}/31`"
//            }
//            + "\n**${context.translate("misc.texts.totalIv")}** ${pokemon.ivPercentage}", // TODO: move IV Sp
//        context.translate(
//          "modules.pokemon.commands.info.embed.title",
//          mapOf(
//            "level" to pokemon.level.toString(),
//            "pokemon" to context.translator.pokemonDisplayName(pokemon),
//            "speciesId" to pokemon.data.formattedSpeciesId
//          )
//        )
//      )
//        .setImage(pokemon.imageUrl)
//        .setFooter(
//          context.translate(
//            "modules.pokemon.commands.info.embed.footer",
//            mapOf(
//              "index" to "${pokemon.index + 1}",
//              "total" to "${userData.nextPokemonIndices.maxOrNull() ?: "Unknown"}"
//            )
//          )
//        )
//        .setTimestamp(Instant.ofEpochMilli(pokemon.timestamp))
//        .setColor(pokemon.data.species.color.colorCode)
//        .build()
//    ).queue()

//    context.reply(
//      context.embedTemplates.normal(
//        """
//        **${context.translate("misc.texts.xp")}**: ${if (pokemon.level >= 100) "Max" else "${pokemon.xp}/${pokemon.requiredXpToLevelUp()}"}
//        **${context.translate("misc.texts.gender")}**: ${context.translator.gender(pokemon)}
//        **${context.translate("misc.texts.nature")}**: ${context.translator.nature(pokemon.nature)}
//        **${context.translator.stat(Stat.hp)}**: ${pokemon.stats.hp} - ${pokemon.ivs.hp}/31
//        **${context.translator.stat(Stat.attack)}**: ${pokemon.stats.attack} - ${pokemon.ivs.attack}/31
//        **${context.translator.stat(Stat.defense)}**: ${pokemon.stats.defense} - ${pokemon.ivs.defense}/31
//        **${context.translator.stat(Stat.specialAttack)}**: ${pokemon.stats.specialAttack} - ${pokemon.ivs.specialAttack}/31
//        **${context.translator.stat(Stat.specialDefense)}**: ${pokemon.stats.specialDefense} - ${pokemon.ivs.specialDefense}/31
//        **${context.translator.stat(Stat.speed)}**: ${pokemon.stats.speed} - ${pokemon.ivs.speed}/31
//        **${context.translate("misc.texts.totalIv")}**: ${pokemon.ivPercentage}
//        **${context.translate("misc.texts.trainerID")}**: ${pokemon.ownerId.asTrainerId}${
//          if (pokemon.trainerId != null) "\n**${
//            context.translate(
//              "misc.texts.originalTrainerID"
//            )
//          }**: ${pokemon.trainerId.asTrainerId}" else ""
//        }
//        ${if (pokemon.heldItemId != 0) "**${context.translate("misc.texts.heldItem")}**: ${(ItemData.getById(pokemon.heldItemId)?.name ?: pokemon.heldItemId)}" else ""}
//      """.trimIndent(),
//        context.translate(
//          "modules.pokemon.commands.info.embed.title",
//          mapOf(
//            "level" to pokemon.level.toString(),
//            "pokemon" to context.translator.pokemonDisplayName(pokemon),
//            "speciesId" to pokemon.data.formattedSpeciesId
//          )
//        )
//      )
//        .setImage(pokemon.imageUrl)
//        .setFooter(
//          context.translate(
//            "modules.pokemon.commands.info.embed.footer",
//            mapOf(
//              "index" to "${pokemon.index + 1}",
//              "total" to "${userData.nextPokemonIndices.maxOrNull() ?: "Unknown"}"
//            )
//          )
//        )
//        .setTimestamp(Instant.ofEpochMilli(pokemon.timestamp))
//        .setColor(pokemon.data.species.color.colorCode)
//        .build()
//    ).queue()
  }

  data class StatInfo(
    val stat: Stat,
    val iv: Int,
    val value: Int
  )
}
