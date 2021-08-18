package xyz.pokecord.test.pokemon.structures

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import xyz.pokecord.bot.core.structures.pokemon.Nature

class NatureTests : DescribeSpec({
  describe("Nature") {
    it("Should return correct data by ID") {
      val hardy = Nature.getById(1)!!

      hardy.decreasedStatId shouldBe 2
      hardy.hatesFlavorId shouldBe 1
      hardy.id shouldBe 1
      hardy.identifier shouldBe "hardy"
      hardy.increasedStatId shouldBe 2
      hardy.likesFlavorId shouldBe 1
      hardy.name?.name shouldBe "Hardy"
    }

    it("Should return correct data by name") {
      val jolly = Nature.getByName("Jolly")!!

      jolly.decreasedStatId shouldBe 4
      jolly.hatesFlavorId shouldBe 2
      jolly.id shouldBe 16
      jolly.identifier shouldBe "jolly"
      jolly.increasedStatId shouldBe 6
      jolly.likesFlavorId shouldBe 3
      jolly.name?.name shouldBe "Jolly"
    }
  }
})
