package xyz.pokecord.bot.core.structures.pokemon

data class EvolutionChain(
  val id: Int,
  val evolvedSpeciesIds: MutableList<Int>
) {
  companion object {
    private val evolutionChains: MutableList<EvolutionChain> = mutableListOf()

    init {
      for (species in Species.items) {
        val evolutionChain = evolutionChains.find { it.id == species.evolutionChainId }
        if (evolutionChain == null) {
          evolutionChains.add(EvolutionChain(species.evolutionChainId, mutableListOf(species.id)))
        } else {
          evolutionChain.evolvedSpeciesIds.add(species.id)
        }
      }
    }

    fun details(evolvedSpeciesId: Int) = PokemonEvolution.items.find { it.evolvedSpeciesId == evolvedSpeciesId }

    fun getById(id: Int) = evolutionChains.find { it.id == id }

    fun of(pokemonId: Int) = evolutionChains.find { it.evolvedSpeciesIds.contains(pokemonId) }
  }
}
