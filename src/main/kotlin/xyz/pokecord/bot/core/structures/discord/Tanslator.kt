package xyz.pokecord.bot.core.structures.discord

import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.discord.base.BaseCommandContext
import xyz.pokecord.bot.core.structures.pokemon.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class Translator(val context: BaseCommandContext) {
  suspend fun numberFormat(number: Number): String {
    val language = context.getLanguage()
    val locale = Locale.forLanguageTag(language.identifier) ?: Locale.forLanguageTag(I18n.Language.EN_US.identifier)
    return NumberFormat.getNumberInstance(locale).format(number)
  }

  suspend fun dateFormat(date: Date): String {
    val language = context.getLanguage()
    val locale = Locale.forLanguageTag(language.identifier) ?: Locale.forLanguageTag(I18n.Language.EN_US.identifier)
    return SimpleDateFormat("EEE, d MMM yyyy", locale).format(date)
  }

  suspend fun pokemonName(pokemon: OwnedPokemon): String? {
    var name =
      pokemon.data.species.forms.find { it.id == pokemon.formId }?.let { pokemonName(it) }
    if (name == null) {
      name = pokemonName(pokemon.data)
    }
    return name
  }

  suspend fun pokemonName(pokemon: Pokemon): String? {
    val form = pokemon.form
    return if (form != null && pokemon.species.forms.size != 1 && form.pokemonId != pokemon.speciesId) {
      pokemonName(form)
    } else {
      pokemonName(pokemon.species)
    }
  }

  suspend fun pokemonName(form: PokemonForm): String? {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return form.names.find { it.languageId == languageId }?.pokemonName?.ifEmpty { null }
  }

  suspend fun pokemonName(species: Species): String? {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return (species.getName(languageId) ?: species.name)?.name
  }

  suspend fun pokemonGenus(pokemon: Pokemon) = pokemonGenus(pokemon.species)
  suspend fun pokemonGenus(species: Species): String? {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return (species.getName(languageId) ?: species.name)?.genus
  }

  suspend fun nature(nature: String) = Nature.getByName(nature)?.let { nature(it) }
  suspend fun nature(nature: Nature): String? {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return (nature.getName(languageId) ?: nature.name)?.name
  }

  suspend fun pokemonDisplayName(pokemon: OwnedPokemon, showNickname: Boolean = true) =
    "${if (showNickname && pokemon.nickname != null) pokemon.nickname else pokemonName(pokemon)}${pokemon.data.getEmoji(pokemon.shiny)}"

  suspend fun habitat(pokemon: Pokemon) = translatePokemonHabitatName(pokemon.species)
  private suspend fun translatePokemonHabitatName(species: Species): Name? {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return species.getHabitatName(languageId) ?: species.habitatName
  }

  suspend fun gender(pokemon: OwnedPokemon) = gender(pokemon.gender)
  suspend fun gender(gender: Int) =
    when (gender) {
      -1 -> context.translate("misc.texts.genderless")
      0 -> context.translate("misc.texts.female")
      1 -> context.translate(
        "misc.texts.male"
      )

      else -> context.translate("misc.texts.unknown")
    }

  suspend fun evolution(pokemon: Pokemon): String {
    val unknown = context.translate("misc.texts.unknown")
    return if (pokemon.nextEvolutions.isEmpty()) if (pokemon.species.evolvesFromSpeciesId != 0) context.translate(
      "misc.texts.lastStage"
    ) else context.translate(
      "misc.texts.noEvolution"
    ) else pokemon.nextEvolutions.map { nextEvolution ->
      Pokemon.getById(
        nextEvolution
      )?.let {
        pokemonName(it)
      } ?: unknown
    }.joinToString(", ").ifBlank { unknown }
  }

  suspend fun prevEvolution(pokemon: Pokemon): String {
    return (if (pokemon.species.evolvesFromSpeciesId != 0) context.translator.pokemonName(Pokemon.getById(pokemon.species.evolvesFromSpeciesId)!!)!!
    else context.translate(
      "misc.texts.firstStage"
    ))
  }

  suspend fun stat(stat: Stat): String {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return stat.getName(languageId)?.name ?: stat.name?.name ?: stat.identifier
  }

  suspend fun type(type: Type): String {
    val languageId = context.getLanguage().pokeApiLanguageId ?: I18n.Language.EN_US.pokeApiLanguageId!!
    return type.getName(languageId)?.name ?: type.name?.name ?: type.identifier
  }

  suspend fun damageClass(id: Int): String {
    return context.translate(if (id == 1) "misc.texts.status" else if (id == 2) "misc.texts.physical" else if (id == 3) "misc.texts.special" else "misc.texts.unknown")
  }
}
