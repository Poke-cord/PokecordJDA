package xyz.pokecord.bot.modules.pokemon.events

import io.prometheus.client.Counter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import xyz.pokecord.bot.core.structures.PrometheusService
import xyz.pokecord.bot.core.structures.discord.MessageCommandContext
import xyz.pokecord.bot.core.structures.discord.base.Event
import kotlin.math.min

object XPGainEvent : Event() {
  override val name = "XPGain"

  private val envFlag = System.getenv("XP_GAIN") != null

  private val lastCountedMessageMap = mutableMapOf<String, Long?>()

  private val processedMessages = Counter
    .build("bot_xp_gain_processed_messages", "Messages processed by the XP Gain Event.")
    .labelNames("hostname", "user_id", "user_tag", "shard", "guild", "channel")
    .register(PrometheusService.registry)

  @Handler
  suspend fun onMessage(context: MessageCommandContext) {
    if (!context.shouldProcess()) return
    if (!envFlag || context.bot.maintenance) return
    if (context.event.message.contentRaw.length <= 2) return
    val prefix = context.getPrefix()
    if (context.event.message.contentRaw.startsWith(prefix, true)) return

    val userData = context.getUserData()
    if (userData.selected == null) return

    val lastMessageAt = lastCountedMessageMap.getOrDefault(context.author.id, null)
    val now = System.currentTimeMillis()
    if (lastMessageAt != null && lastMessageAt + 5000 > now) return
    lastCountedMessageMap[context.author.id] = now

    val selectedPokemon = context.bot.database.pokemonRepository.getPokemonById(userData.selected!!)
    if (selectedPokemon == null || selectedPokemon.level >= 100) return

    var xp = min(context.event.message.contentRaw.replace("[ \\n]".toRegex(), "").length * 10, 1000)
    val percentage = when (userData.donationTier) {
      6 -> 20
      5 -> 15
      4 -> 10
      3 -> 5
      else -> 0
    }
    xp += ((xp / 100.0) * percentage).toInt()

    val oldPokemonName = context.translator.pokemonDisplayName(selectedPokemon)

    val (leveledUp, evolved) = context.bot.database.pokemonRepository.levelUpAndEvolveIfPossible(
      selectedPokemon,
      gainedXp = xp
    )

    if (evolved) {
      context.bot.database.userRepository.addDexCatchEntry(userData, selectedPokemon)
    }

    processedMessages.labels(
      module.bot.hostname,
      context.author.id,
      context.author.asTag,
      context.jda.shardInfo.shardId.toString(),
      context.guild?.id ?: "DM",
      context.channel.id
    ).inc()

    if (context.isFromGuild) {
      val guildData = context.getGuildData()
      if (guildData?.levelUpMessagesSilenced == true) return
      if (!context.guild!!.selfMember.hasPermission(
          context.channel as GuildChannel,
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_READ,
          Permission.MESSAGE_WRITE,
          Permission.MESSAGE_EMBED_LINKS
        )
      ) return
    }

    val embedBuilder =
      when {
        evolved -> context.embedTemplates.normal(
          context.translate(
            "misc.embeds.evolved.description",
            mapOf(
              "user" to context.author.asMention,
              "pokemon" to oldPokemonName,
              "evolvedPokemon" to context.translator.pokemonDisplayName(selectedPokemon),
              "level" to selectedPokemon.level.toString()
            )
          ),
          context.translate("misc.embeds.evolved.title")
        )
        leveledUp -> context.embedTemplates.normal(
          context.translate(
            "misc.embeds.levelUp.description",
            mapOf(
              "user" to context.author.asMention,
              "pokemon" to context.translator.pokemonDisplayName(selectedPokemon),
              "level" to selectedPokemon.level.toString()
            )
          ),
          context.translate("misc.embeds.levelUp.title")
        )
        else -> null
      }

    embedBuilder?.let {
      it.setColor(selectedPokemon.data.species.color.colorCode)
      context.channel.sendMessageEmbeds(it.build()).queue()
    }
  }
}
