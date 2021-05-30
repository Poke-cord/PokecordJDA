package xyz.pokecord.bot.core.structures.pokemon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.utils.Json
import kotlin.system.exitProcess

@Serializable
data class Nature(
  val id: Int,
  val identifier: String,
  val decreasedStatId: Int,
  val increasedStatId: Int,
  val hatesFlavorId: Int,
  val likesFlavorId: Int,
  private val gameIndex: Int,
  @SerialName("name") private val generatedName: String
) {
  @Transient
  val name = getName()

  fun getName(languageId: Int = 9) =
    names.find { it.id == id && it.languageId == languageId }

  companion object {
    private val items: List<Nature>
    private val names: List<Name>

    init {
      val namesStream = Nature::class.java.getResourceAsStream("/data/nature_names.json")
      if (namesStream == null) {
        println("Nature names not found. Exiting...")
        exitProcess(0)
      }
      val namesJson = namesStream.readAllBytes().decodeToString()
      names = Json.decodeFromString(namesJson)
      val stream = Nature::class.java.getResourceAsStream("/data/natures.json")
      if (stream == null) {
        println("Nature data not found. Exiting...")
        exitProcess(0)
      }
      val json = stream.readAllBytes().decodeToString()
      items = Json.decodeFromString(json)
    }

    fun getById(id: Int) = items.find { it.id == id }

    fun getByName(name: String): Nature? {
      val id = names.find { it.name.equals(name, true) }?.id ?: return null
      return getById(id)
    }

    fun random() = items.random().name?.name
  }
}
