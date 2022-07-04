package xyz.pokecord.test.pokemon.structures

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import xyz.pokecord.bot.core.managers.database.models.OwnedPokemon
import xyz.pokecord.bot.core.structures.pokemon.Nature
import xyz.pokecord.bot.core.structures.pokemon.Stat

class StatTests : DescribeSpec({
  describe("Stat") {
    it("Should return correct data for HP") {
      Stat.hp.damageClassId shouldBe 0
      Stat.hp.gameIndex shouldBe 1
      Stat.hp.id shouldBe 1
      Stat.hp.identifier shouldBe "hp"
      Stat.hp.isBattleOnly shouldBe false
      Stat.hp.name?.name shouldBe "HP"
    }

    it("Should return correct data for Speed") {
      Stat.speed.damageClassId shouldBe 0
      Stat.speed.gameIndex shouldBe 4
      Stat.speed.id shouldBe 6
      Stat.speed.identifier shouldBe "speed"
      Stat.speed.isBattleOnly shouldBe false
      Stat.speed.name?.name shouldBe "Speed"
    }

    it("Should return correct values from the methods for Attack") {
      Stat.attack.getBaseEffortValue(1) shouldBe 0
      Stat.attack.getBaseValue(1) shouldBe 49
      OwnedPokemon.getStatValue(1, 50, Stat.attack, Nature.getByName("jolly")!!, 25, 0, 0) shouldBe 66
    }

    it("Should return correct values from the methods for HP") {
      Stat.hp.getBaseEffortValue(1) shouldBe 0
      Stat.hp.getBaseValue(1) shouldBe 45
      OwnedPokemon.getStatValue(1, 50, Stat.hp, Nature.getByName("calm")!!, 29, 0, 0) shouldBe 119
    }

    it("Should return correct values from the methods for neutral natures") {
      Stat.defense.getBaseEffortValue(1) shouldBe 0
      Stat.defense.getBaseValue(1) shouldBe 49
      OwnedPokemon.getStatValue(1, 50, Stat.defense, Nature.getByName("serious")!!, 29, 0, 0) shouldBe 68
    }
  }
})
