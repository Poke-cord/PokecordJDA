package xyz.pokecord.test.pokemon.structures

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import xyz.pokecord.bot.utils.extensions.parseTime

class StringExtensionTests : DescribeSpec({
  describe("StringExtension") {
    it("Should parse single inputs correctly") {
      "2w".parseTime() shouldBe 1209600000L
      "3d 20s".parseTime() shouldBe 259220000L
      "59m 10s".parseTime() shouldBe 3550000L
      "5w 2d 3h 4m 25s".parseTime() shouldBe 3207865000L
    }
  }
})