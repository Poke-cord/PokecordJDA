package xyz.pokecord.test.pokemon.structures

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import xyz.pokecord.bot.core.structures.pokemon.Type

class TypeTests : DescribeSpec({
  describe("Type") {
    it("Should return correct data by ID") {
      val normal = Type.getById(1)!!
      normal.damageClassId shouldBe 2
      normal.generationId shouldBe 1
      normal.id shouldBe 1
      normal.identifier shouldBe "normal"
      normal.name?.name shouldBe "Normal"
    }

    it("Should return correct data by Name") {
      val fire = Type.getByName("Fire")!!
      fire.damageClassId shouldBe 3
      fire.generationId shouldBe 1
      fire.id shouldBe 10
      fire.identifier shouldBe "fire"
      fire.name?.name shouldBe "Fire"
    }

    it("Should return correct data for Shadow") {
      val shadow = Type.getByName("Shadow")!!
      shadow.damageClassId shouldBe 0
      shadow.generationId shouldBe 3
      shadow.id shouldBe 10002
      shadow.identifier shouldBe "shadow"
      shadow.name?.name shouldBe "Shadow"
    }
  }
})
