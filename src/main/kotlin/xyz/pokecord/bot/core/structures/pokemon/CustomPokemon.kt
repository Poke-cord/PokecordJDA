package xyz.pokecord.bot.core.structures.pokemon

object CustomPokemon {
  fun init() {
    val spinda = Pokemon.getById(327)!!
    val bluesSpinda = spinda.copy(
      id = 999900001,
      identifier = "blues-spinda",
      name = "Blue's Spinda",
      order = 9999,
      isDefault = false,
      formName = "Blue's Spinda",
    )
    registerCustomPokemon(
      bluesSpinda,
      "blues-custom-pokemon",
      "Blue's Custom Pokemon",
      PokemonType(bluesSpinda.id, spinda.types.map { it.id }),
      PokemonStat.getByPokemonId(spinda.id).map {
        it.copy(id = bluesSpinda.id)
      }
    )
  }

  private fun registerCustomPokemon(
    pokemon: Pokemon,
    formIdentifier: String,
    formName: String,
    type: PokemonType,
    stats: List<PokemonStat>,
    isBattleOnly: Boolean = false,
    isMega: Boolean = false,
  ) {
    // Custom Pokemon Entry
    Pokemon.addEntry(pokemon)

    // Custom Pokemon Type Entry
    Pokemon.addTypeEntry(type)

    // Custom Pokemon Stat Entries
    stats.forEach { PokemonStat.addEntry(it) }

    // Custom Form Entry
    PokemonForm.addEntry(
      PokemonForm(
        pokemon.id,
        pokemon.identifier,
        formIdentifier,
        pokemon.id,
        9999,
        pokemon.isDefault,
        isBattleOnly,
        isMega,
        9999,
        9999,
      )
    )

    // Custom Form Name Entry
    PokemonFormName.addEntry(
      PokemonFormName(
        pokemon.id,
        9,
        formName,
        pokemon.name,
      )
    )
  }
}