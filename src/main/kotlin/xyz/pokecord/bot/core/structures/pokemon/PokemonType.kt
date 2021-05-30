package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.Serializable

@Serializable
data class PokemonType(val id: Int, val types: List<Int>)
