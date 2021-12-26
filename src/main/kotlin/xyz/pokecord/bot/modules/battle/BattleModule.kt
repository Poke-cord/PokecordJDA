package xyz.pokecord.bot.modules.battle

import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.Component
import xyz.pokecord.bot.core.managers.database.models.Battle
import xyz.pokecord.bot.core.structures.discord.Bot
import xyz.pokecord.bot.core.structures.discord.base.Module
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import xyz.pokecord.bot.core.structures.pokemon.Stat
import xyz.pokecord.bot.modules.battle.commands.BattleCommand
import xyz.pokecord.bot.modules.battle.commands.MovesCommand
import xyz.pokecord.bot.modules.battle.events.BattleActionEvent
import xyz.pokecord.bot.modules.battle.events.BattleRequestActionEvent
import xyz.pokecord.bot.utils.ImageUtils.flipHorizontally
import xyz.pokecord.bot.utils.ImageUtils.loadImage
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import kotlin.reflect.full.primaryConstructor

class BattleModule(bot: Bot) : Module(
  bot,
  arrayOf(
    BattleCommand,
    MovesCommand,
    MovesCommand.TeachCommand,
  ),
  arrayOf(
    BattleActionEvent,
    BattleRequestActionEvent
  )
) {
  override val name = "Battle"

  sealed class Buttons(
    private val buttonId: String,
    private vararg val params: String
  ) {
    sealed class BattleRequest(buttonId: String, val initiatedChannelId: String, val initiatedAtMillis: String) :
      Buttons(buttonId, initiatedChannelId, initiatedAtMillis) {
      class Accept(initiatedChannelId: String, initiatedAtMillis: String) :
        BattleRequest(ACCEPT_BATTLE_REQUEST_BUTTON_ID, initiatedChannelId, initiatedAtMillis)

      class Reject(initiatedChannelId: String, initiatedAtMillis: String) :
        BattleRequest(REJECT_BATTLE_REQUEST_BUTTON_ID, initiatedChannelId, initiatedAtMillis)
    }

    sealed class BattleAction(buttonId: String, val battleId: String, vararg params: String) :
      Buttons(buttonId, battleId, *params) {
      class ChooseMove(battleId: String, val moveId: String) :
        BattleAction(BATTLE_CHOOSE_MOVE_BUTTON_ID, battleId, moveId)

      class UseMove(battleId: String) : BattleAction(BATTLE_USE_MOVE_BUTTON_ID, battleId)
      class EndBattle(battleId: String) : BattleAction(BATTLE_END_BUTTON_ID, battleId)
    }

    override fun toString(): String {
      return "${buttonId}-${params.joinToString("-")}"
    }

    companion object {
      fun fromComponentId(buttonId: String): Buttons? {
        val split = buttonId.split("-")
        val buttonIdClass = when (split[0]) {
          ACCEPT_BATTLE_REQUEST_BUTTON_ID -> BattleRequest.Accept::class
          REJECT_BATTLE_REQUEST_BUTTON_ID -> BattleRequest.Reject::class
          BATTLE_USE_MOVE_BUTTON_ID -> BattleAction.UseMove::class
          BATTLE_CHOOSE_MOVE_BUTTON_ID -> BattleAction.ChooseMove::class
          BATTLE_END_BUTTON_ID -> BattleAction.EndBattle::class
          else -> null
        } ?: return null
        val args = split.drop(1)
        return buttonIdClass.primaryConstructor?.call(*args.toTypedArray())
      }

      fun getBattleActionRow(battleId: String): List<Component> {
        return listOf(
          Button.primary(BattleAction.UseMove(battleId).toString(), "Use a move"),
          Button.secondary(BattleAction.EndBattle(battleId).toString(), "End battle")
        )
      }

      fun getBattleRequestActionRow(battleRequest: Battle.Request): List<Component> {
        return listOf(
          Button.success(
            BattleRequest.Accept(
              battleRequest.initiatedChannelId,
              battleRequest.initiatedAtMillis.toString()
            ).toString(), "Accept"
          ),
          Button.danger(
            BattleRequest.Reject(
              battleRequest.initiatedChannelId,
              battleRequest.initiatedAtMillis.toString()
            ).toString(), "Reject"
          )
        )
      }
    }
  }

  companion object {
    const val BATTLE_TIMEOUT_MILLIS: Long = 1000 * 60 * 5

    private const val ACCEPT_BATTLE_REQUEST_BUTTON_ID = "accept_battle_request"
    private const val REJECT_BATTLE_REQUEST_BUTTON_ID = "reject_battle_request"
    private const val BATTLE_USE_MOVE_BUTTON_ID = "battle_use_move"
    private const val BATTLE_CHOOSE_MOVE_BUTTON_ID = "battle_choose_move"
    private const val BATTLE_END_BUTTON_ID = "battle_end"

    fun getBattleImage(battle: Battle): ByteArray {
      val backgroundImage = loadImage("background.png")
      val standImage = loadImage("stand.png")
      val leftHpImage = loadImage("left_hp.png")
      val rightHpImage = loadImage("right_hp.png")
      val hpBarImage = loadImage("hp_bar.png")

      val leftPokemon = Pokemon.getById(battle.initiator.pokemonId)!!
      val rightPokemon = Pokemon.getById(battle.partner.pokemonId)!!

      val leftPokemonImage =
        loadImage(URL(leftPokemon.imageUrl))
          .run { if (leftPokemon.leftFacing) this.flipHorizontally() else this }
      val rightPokemonImage =
        loadImage(URL(rightPokemon.imageUrl))
          .run { if (rightPokemon.rightFacing) this.flipHorizontally() else this }

      val image = BufferedImage(800, 480, BufferedImage.TYPE_INT_ARGB)
      image.graphics.apply {
        drawImage(backgroundImage, 0, 0, null)

        drawImage(standImage, image.width / 2 / 2 - standImage.width / 2, 350, null)
        drawImage(
          standImage,
          image.width / 2 + image.width / 2 / 2 - standImage.width / 2,
          350,
          null
        )

        drawImage(
          leftPokemonImage,
          (image.width / 2 / 2 - leftPokemonImage.width / 2.5 + 105).roundToInt(),
          200,
          leftPokemonImage.width / (leftPokemonImage.width / 200),
          leftPokemonImage.height / (leftPokemonImage.height / 200),
          null
        )

        drawImage(
          rightPokemonImage,
          (image.width / 2 +
              image.width / 2 / 2 -
              rightPokemonImage.width / 2.5 +
              105).roundToInt(),
          200,
          rightPokemonImage.width / (rightPokemonImage.width / 200),
          rightPokemonImage.height / (rightPokemonImage.height / 200),
          null
        )

        drawImage(leftHpImage, image.width / 2 / 2 - standImage.width / 2 + 25, 100, null)

        drawImage(
          rightHpImage,
          image.width / 2 + image.width / 2 / 2 - standImage.width / 2 + 25,
          100,
          null
        )

        drawImage(
          hpBarImage,
          image.width / 2 / 2 - hpBarImage.width / 2 + 22,
          114,
          ((battle.initiator.pokemonStats.hp.toDouble() / Stat.hp.getBaseValue(leftPokemon.id)!!) * 96).roundToInt(),
          6,
          null
        )
        drawImage(
          hpBarImage,
          image.width / 2 + image.width / 2 / 2 - hpBarImage.width / 2 + 52,
          115,
          ((battle.partner.pokemonStats.hp.toDouble() / Stat.hp.getBaseValue(rightPokemon.id)!!) * 96).roundToInt(),
          6,
          null
        )
      }

      return ByteArrayOutputStream().let {
        ImageIO.write(image, "png", it)
        it.toByteArray()
      }
    }
  }
}
