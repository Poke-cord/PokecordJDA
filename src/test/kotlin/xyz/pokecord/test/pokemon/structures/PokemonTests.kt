package xyz.pokecord.test.pokemon.structures

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import xyz.pokecord.bot.core.structures.pokemon.Pokemon

class PokemonTests : DescribeSpec({
  describe("Pokémon") {
    it("Should return correct data by ID") {
      val bulbasaur = Pokemon.getById(1)!!

      // Basic data test
      bulbasaur.baseExp shouldBe 64
      bulbasaur.species.color.colorCode shouldBe 7915600
      bulbasaur.formName shouldBe null
      bulbasaur.height shouldBe 7
      bulbasaur.id shouldBe 1
      bulbasaur.identifier shouldBe "bulbasaur"
      bulbasaur.isDefault shouldBe true
      bulbasaur.name shouldBe "Bulbasaur"
      bulbasaur.order shouldBe 1
      bulbasaur.speciesId shouldBe 1
      bulbasaur.weight shouldBe 69

      // Evolutions Data Test
      bulbasaur.nextEvolutions.size shouldBe 1
      bulbasaur.nextEvolutions[0] shouldBe 2

      // Moves data test
      // Basic moves data test
      bulbasaur.moves.size shouldBe 84
      val razorLeaf = bulbasaur.moves.find { it.moveData.name == "Razor Leaf" }!!
      razorLeaf.moveData.accuracy shouldBe 95
      razorLeaf.moveData.contestEffectId shouldBe 2
      razorLeaf.moveData.damageClassId shouldBe 2
      razorLeaf.moveData.effectChance shouldBe 0
      razorLeaf.moveData.effectId shouldBe 44
      razorLeaf.moveData.generationId shouldBe 1
      razorLeaf.moveData.name shouldBe "Razor Leaf"
      razorLeaf.moveData.power shouldBe 55
      razorLeaf.moveData.pp shouldBe 25
      razorLeaf.moveData.priority shouldBe 0
      razorLeaf.moveData.superContestEffectId shouldBe 5
      razorLeaf.moveData.targetId shouldBe 11
      razorLeaf.moveData.typeId shouldBe 12

      razorLeaf.id shouldBe 75
      razorLeaf.moveData.identifier shouldBe "razor-leaf"
      razorLeaf.moveMethodId shouldBe 1
      razorLeaf.order shouldBe 0
      razorLeaf.requiredLevel shouldBe 19
      razorLeaf.versionGroupId shouldBe 18

      // Move meta data test
      razorLeaf.moveMeta.ailmentChance shouldBe 0
      razorLeaf.moveMeta.criticalRate shouldBe 1
      razorLeaf.moveMeta.drain shouldBe 0
      razorLeaf.moveMeta.flinchChance shouldBe 0
      razorLeaf.moveMeta.healing shouldBe 0
      razorLeaf.moveMeta.id shouldBe 75
      razorLeaf.moveMeta.maxHits shouldBe 0
      razorLeaf.moveMeta.maxTurns shouldBe 0
      razorLeaf.moveMeta.metaAilmentId shouldBe 0
      razorLeaf.moveMeta.metaCategoryId shouldBe 0
      razorLeaf.moveMeta.minHits shouldBe 0
      razorLeaf.moveMeta.minTurns shouldBe 0
      razorLeaf.moveMeta.statChance shouldBe 0

      // Move type data test
      razorLeaf.moveData.type.damageClassId shouldBe 3
      razorLeaf.moveData.type.generationId shouldBe 1
      razorLeaf.moveData.type.id shouldBe 12
      razorLeaf.moveData.type.identifier shouldBe "grass"
      razorLeaf.moveData.type.name?.name shouldBe "Grass"

      // Types data test
      bulbasaur.types.size shouldBe 2

      // Type 1 test
      bulbasaur.types[0].id shouldBe 12
      bulbasaur.types[0].identifier shouldBe "grass"
      bulbasaur.types[0].generationId shouldBe 1
      bulbasaur.types[0].damageClassId shouldBe 3
      bulbasaur.types[0].name?.name shouldBe "Grass"

      // Type 2 test
      bulbasaur.types[1].id shouldBe 4
      bulbasaur.types[1].identifier shouldBe "poison"
      bulbasaur.types[1].generationId shouldBe 1
      bulbasaur.types[1].damageClassId shouldBe 2
      bulbasaur.types[1].name?.name shouldBe "Poison"

      // Species data test
      // Basic species data test
      bulbasaur.species.name?.genus shouldBe "Seed Pokémon"
      bulbasaur.species.baseHappiness shouldBe 70
      bulbasaur.species.captureRate shouldBe 45
      bulbasaur.species.colorId shouldBe 5
      bulbasaur.species.evolutionChainId shouldBe 1
      bulbasaur.species.evolvesFromSpeciesId shouldBe 0
      bulbasaur.species.formsSwitchable shouldBe false
      bulbasaur.species.genderRate shouldBe 1
      bulbasaur.species.generationId shouldBe 1
      bulbasaur.species.growthRateId shouldBe 4
      bulbasaur.species.habitatId shouldBe 3
      bulbasaur.species.hasGenderDifferences shouldBe false
      bulbasaur.species.hatchCounter shouldBe 20
      bulbasaur.species.identifier shouldBe "bulbasaur"
      bulbasaur.species.isBaby shouldBe false
      bulbasaur.species.name?.name shouldBe "Bulbasaur"
      bulbasaur.species.order shouldBe 1
      bulbasaur.species.shapeId shouldBe 8

      // Species habitat test
      bulbasaur.species.habitatName?.id shouldBe 3
      bulbasaur.species.habitatName?.languageId shouldBe 9
      bulbasaur.species.habitatName?.name shouldBe "Grassland"
    }

    it("Should return correct data by name") {
      val zeraora = Pokemon.getByName("zeraora")!!

      // Basic data test
      zeraora.baseExp shouldBe 270
      zeraora.species.color.colorCode shouldBe 16776960
      zeraora.formName shouldBe null
      zeraora.height shouldBe 15
      zeraora.id shouldBe 807
      zeraora.identifier shouldBe "zeraora"
      zeraora.isDefault shouldBe true
      zeraora.name shouldBe "Zeraora"
      zeraora.order shouldBe 964
      zeraora.speciesId shouldBe 807
      zeraora.weight shouldBe 445

      // Evolutions Data Test
      zeraora.nextEvolutions.size shouldBe 0

      // Moves data test
      // Basic moves data test
      zeraora.moves.size shouldBe 49
      val plasmaFists = zeraora.moves.find { it.moveData.name == "Plasma Fists" }!!
      plasmaFists.moveData.accuracy shouldBe 100
      plasmaFists.moveData.contestEffectId shouldBe 0
      plasmaFists.moveData.damageClassId shouldBe 2
      plasmaFists.moveData.effectChance shouldBe 0
      plasmaFists.moveData.effectId shouldBe 1
      plasmaFists.moveData.generationId shouldBe 7
      plasmaFists.moveData.identifier shouldBe "plasma-fists"
      plasmaFists.moveData.name shouldBe "Plasma Fists"
      plasmaFists.moveData.power shouldBe 100
      plasmaFists.moveData.pp shouldBe 15
      plasmaFists.moveData.priority shouldBe 0
      plasmaFists.moveData.superContestEffectId shouldBe 0
      plasmaFists.moveData.targetId shouldBe 10
      plasmaFists.moveData.typeId shouldBe 13
      plasmaFists.id shouldBe 721
      plasmaFists.moveMethodId shouldBe 1
      plasmaFists.order shouldBe 0
      plasmaFists.requiredLevel shouldBe 43
      plasmaFists.versionGroupId shouldBe 18

      // Move meta data test
      plasmaFists.moveMeta.ailmentChance shouldBe 0
      plasmaFists.moveMeta.criticalRate shouldBe 0
      plasmaFists.moveMeta.drain shouldBe 0
      plasmaFists.moveMeta.flinchChance shouldBe 0
      plasmaFists.moveMeta.healing shouldBe 0
      plasmaFists.moveMeta.id shouldBe 721
      plasmaFists.moveMeta.maxHits shouldBe 0
      plasmaFists.moveMeta.maxTurns shouldBe 0
      plasmaFists.moveMeta.metaAilmentId shouldBe 0
      plasmaFists.moveMeta.metaCategoryId shouldBe 0
      plasmaFists.moveMeta.minHits shouldBe 0
      plasmaFists.moveMeta.minTurns shouldBe 0
      plasmaFists.moveMeta.statChance shouldBe 0

      // Move type data test
      plasmaFists.moveData.type.damageClassId shouldBe 3
      plasmaFists.moveData.type.generationId shouldBe 1
      plasmaFists.moveData.type.id shouldBe 13
      plasmaFists.moveData.type.identifier shouldBe "electric"
      plasmaFists.moveData.type.name?.name shouldBe "Electric"

      // Types data test
      zeraora.types.size shouldBe 1

      // Type 1 test
      zeraora.types[0].id shouldBe 13
      zeraora.types[0].identifier shouldBe "electric"
      zeraora.types[0].generationId shouldBe 1
      zeraora.types[0].damageClassId shouldBe 3
      zeraora.types[0].name?.name shouldBe "Electric"

      // Species data test
      // Basic species data test
      zeraora.species.name?.genus shouldBe "Thunderclap Pokémon"
      zeraora.species.baseHappiness shouldBe 0
      zeraora.species.captureRate shouldBe 3
      zeraora.species.colorId shouldBe 10
      zeraora.species.evolutionChainId shouldBe 427
      zeraora.species.evolvesFromSpeciesId shouldBe 0
      zeraora.species.formsSwitchable shouldBe false
      zeraora.species.genderRate shouldBe -1
      zeraora.species.generationId shouldBe 7
      zeraora.species.growthRateId shouldBe 1
      zeraora.species.habitatId shouldBe 0
      zeraora.species.hasGenderDifferences shouldBe false
      zeraora.species.hatchCounter shouldBe 120
      zeraora.species.identifier shouldBe "zeraora"
      zeraora.species.isBaby shouldBe false
      zeraora.species.name?.name shouldBe "Zeraora"
      zeraora.species.order shouldBe 807
      zeraora.species.shapeId shouldBe 12

      // Species habitat test
      zeraora.species.habitatName shouldBe null
    }
  }
})
