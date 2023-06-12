package xyz.pokecord.bot.core.structures.pokemon

object CustomPokemon {
  fun init() {
    val charizard = Pokemon.getByName("Charizard")!!
    val prideCharizardV1 = charizard.copy(
      id = 100000001,
      identifier = "pride-charizard-v1",
      name = "Pride Charizard V1",
      isDefault = false,
      formName = "Pride Charizard V1",
    )
    val prideCharizardV2 = charizard.copy(
      id = 100000002,
      identifier = "pride-charizard-v2",
      name = "Pride Charizard V2",
      isDefault = false,
      formName = "Pride Charizard V2",
    )

    registerCustomPokemon(
      prideCharizardV1,
      "pride-charizard-v1",
      "Pride Charizard V1",
      PokemonType(prideCharizardV1.id, charizard.types.map { it.id }),
      PokemonStat.getByPokemonId(charizard.id).map {
        it.copy(id = prideCharizardV1.id)
      }
    )
    registerCustomPokemon(
      prideCharizardV2,
      "pride-charizard-v2",
      "Pride Charizard V2",
      PokemonType(prideCharizardV2.id, charizard.types.map { it.id }),
      PokemonStat.getByPokemonId(charizard.id).map {
        it.copy(id = prideCharizardV2.id)
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