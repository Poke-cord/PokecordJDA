package xyz.pokecord.bot.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import xyz.pokecord.bot.core.structures.pokemon.Pokemon
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.random.Random

class DistributedRandomNumberGenerator<T> {
  private val distribution: MutableMap<T, Double?>
  private var distSum = 0.0
  fun add(value: T, distribution: Double) {
    if (this.distribution[value] != null) {
      distSum -= this.distribution[value]!!
    }
    this.distribution[value] = distribution
    distSum += distribution
  }

  val distributedRandomNumber: T?
    get() {
      val rand = Math.random()
      val ratio = 1.0f / distSum
      var tempDist = 0.0
      for (i in distribution.keys) {
        tempDist += distribution[i]!!
        if (rand / ratio <= tempDist) {
          return i
        }
      }
      return null
    }

  init {
    distribution = HashMap()
  }
}

class WeightedRandomizer<T>(
  private val population: List<T>,
  weights: List<Int>?
) {
  private val cumWeights = weights?.let { accumulate(it) }

  private fun accumulate(items: List<Int>): MutableList<Int> {
    val newList = mutableListOf<Int>()
    var currentSum = 0
    for (item in items) {
      newList.add(item + currentSum)
      currentSum += item
    }
    return newList
  }

  private fun bisect(a: List<Int>, x: Int, lo: Int = 0, hi: Int = a.size): Int {
    var h = hi
    var l = lo

    while (l < h) {
      val mid = (l + h) / 2
      if (x < a[mid]) {
        h = mid
      } else {
        l = mid + 1
      }
    }
    return l
  }

  fun randomChoice(): T {
    val index = when (cumWeights) {
      null -> floor(Random.nextDouble() * population.size).roundToInt()
      else -> {
        val total = cumWeights.last()
        bisect(
          cumWeights, (Random.nextDouble() * total).roundToInt(), 0, population.size - 1
        )
      }
    }
    return population[index]
  }
}

//@Serializable
//data class PokemonSpawnWeight(
//  val weight: Int,
//  val pokemon: List<String>
//)

@Serializable
data class PokemonSpawnWeight(
  val id: Int,
  var spawnWeight: Int
)

object SpawnSimulator {
  private fun simulateSpawnWeight() {
//    val jsonStr = """
//      [{"weight":3,"pokemon":["Mew","Celebi","Jirachi","Deoxys","Phione","Manaphy","Darkrai","Shaymin","Arceus","Victini","Keldeo","Meloetta","Genesect","Diancie","Hoopa","Volcanion","Magearna","Marshadow","Zeraora"]},{"weight":6,"pokemon":["Articuno","Zapdos","Moltres","Mewtwo","Raikou","Entei","Suicune","Lugia","Ho Oh","Regirock","Regice","Registeel","Latias","Latios","Kyogre","Groudon","Rayquaza","Uxie","Mesprit","Azelf","Dialga","Palkia","Heatran","Regigigas","Giratina","Cresselia","Cobalion","Terrakion","Virizion","Tornadus","Thundurus","Reshiram","Zekrom","Landorus","Kyurem","Xerneas","Yveltal","Zygarde","Type: Null","Silvally","Tapu Koko","Tapu Lele","Tapu Bulu","Tapu Fini","Cosmog","Cosmoem","Solgaleo","Lunala","Necrozma"]},{"weight":12,"pokemon":["Nihilego","Buzzwole","Pheromosa","Xurkitree","Celesteela","Kartana","Guzzlord","Poipole","Naganadel","Stakataka","Blacephalon"]},{"weight":64,"pokemon":["Dragonite","Tyranitar","Aggron","Salamence","Metagross","Garchomp","Hydreigon","Goodra","Kommo O"]},{"weight":96,"pokemon":["Venusaur","Charizard","Blastoise","Meganium","Typhlosion","Feraligatr","Sceptile","Blaziken","Swampert","Torterra","Infernape","Empoleon","Serperior","Emboar","Samurott","Chesnaught","Delphox","Greninja","Decidueye","Incineroar","Primarina"]},{"weight":256,"pokemon":["Butterfree","Beedrill","Pidgeot","Raichu","Nidoqueen","Nidoking","Clefable","Wigglytuff","Crobat","Vileplume","Bellossom","Poliwrath","Politoed","Alakazam","Machamp","Victreebel","Golem","Magnezone","Gengar","Rhyperior","Blissey","Kingdra","Electivire","Magmortar","Porygon Z","Dragonair","Togekiss","Ampharos","Azumarill","Jumpluff","Mamoswine","Pupitar","Beautifly","Dustox","Ludicolo","Shiftry","Gardevoir","Gallade","Slaking","Exploud","Lairon","Roserade","Flygon","Dusknoir","Chimecho","Walrein","Shelgon","Metang","Staraptor","Luxray","Gabite","Stoutland","Unfezant","Gigalith","Conkeldurr","Seismitoad","Leavanny","Scolipede","Krookodile","Gothitelle","Reuniclus","Vanilluxe","Klinklang","Eelektross","Chandelure","Haxorus","Zweilous","Talonflame","Vivillon","Florges","Aegislash","Sliggoo","Toucannon","Vikavolt","Tsareena","Hakamo O"]},{"weight":384,"pokemon":["Ivysaur","Charmeleon","Wartortle","Pichu","Cleffa","Igglybuff","Tyrogue","Happiny","Mime Jr","Smoochum","Elekid","Magby","Munchlax","Bayleef","Quilava","Croconaw","Togepi","Azurill","Bonsly","Wynaut","Mantyke","Grovyle","Combusken","Marshtomp","Budew","Chingling","Grotle","Monferno","Prinplup","Riolu","Servine","Pignite","Dewott","Quilladin","Braixen","Frogadier","Dartrix","Torracat","Brionne"]},{"weight":512,"pokemon":["Metapod","Kakuna","Pidgeotto","Raticate","Fearow","Arbok","Pikachu","Sandslash","Nidorina","Nidorino","Clefairy","Ninetales","Jigglypuff","Golbat","Gloom","Parasect","Venomoth","Dugtrio","Persian","Golduck","Primeape","Arcanine","Poliwhirl","Kadabra","Machoke","Weepinbell","Tentacruel","Graveler","Rapidash","Slowbro","Slowking","Mangeton","Dodrio","Dewgong","Muk","Cloyster","Haunter","Steelix","Hypno","Kingler","Electrode","Exeggutor","Marowak","Hitmonlee","Hitmonchan","Hitmontop","Lickilicky","Weezing","Rhydon","Chansey","Tangrowth","Seadra","Seaking","Starmie","Mr Mime","Scizor","Jynx","Electabuzz","Magmar","Gyarados","Vaporeon","Jolteon","Flareon","Espeon","Umbreon","Leafeon","Glaceon","Sylveon","Porygon2","Omastar","Kabutops","Snorlax","Dratini","Furret","Noctowl","Ledian","Ariados","Lanturn","Togetic","Xatu","Flaaffy","Marill","Sudowoodo","Skiploom","Ambipom","Sunflora","Yanmega","Quagsire","Honchkrow","Mismagius","Wobbuffet","Forretress","Gliscor","Granbull","Weavile","Ursaring","Magcargo","Piloswine","Octillery","Mantine","Houndoom","Donphan","Larvitar","Mightyena","Linoone","Silcoon","Cascoon","Lombre","Nuzleaf","Swellow","Pelipper","Kirlia","Masquerain","Breloom","Vigoroth","Ninjask","Shedinja","Loudred","Hariyama","Probopass","Delcatty","Aron","Medicham","Manectric","Roselia","Swalot","Sharpedo","Wailord","Camerupt","Grumpig","Vibrava","Cacturne","Altaria","Whiscash","Crawdaunt","Claydol","Cradily","Armaldo","Milotic","Banette","Dusclops","Glalie","Froslass","Sealeo","Huntail","Gorebyss","Bagon","Beldum","Staravia","Bibarel","Kricketune","Luxio","Rampardos","Bastiodon","Wormadam","Mothim","Vespiquen","Floatzel","Cherrim","Gastrodon","Drifblim","Lopunny","Purugly","Skuntank","Bronzong","Gible","Lucario","Hippowdon","Drapion","Toxicroak","Lumineon","Abomasnow","Watchog","Herdier","Liepard","Simisage","Simisear","Simipour","Musharna","Tranquill","Zebstrika","Boldore","Swoobat","Excadrill","Gurdurr","Palpitoad","Swadloon","Whirlipede","Whimsicott","Lilligant","Krokorok","Darmanitan","Crustle","Scrafty","Cofagrigus","Carracosta","Archeops","Garbodor","Zoroark","Cinccino","Gothorita","Duosion","Swanna","Vanillish","Sawsbuck","Escavalier","Amoonguss","Jellicent","Galvantula","Ferrothorn","Klang","Eelektrik","Beheeyem","Lampent","Fraxure","Beartic","Accelgor","Mienshao","Golurk","Bisharp","Braviary","Mandibuzz","Deino","Volcarona","Diggersby","Fletchinder","Spewpa","Pyroar","Floette","Gogoat","Pangoro","Meowstic","Doublade","Aromatisse","Slurpuff","Malamar","Barbaracle","Dragalge","Clawitzer","Heliolisk","Tyrantrum","Aurorus","Goomy","Trevenant","Gourgeist","Avalugg","Noivern","Trumbeak","Gumshoos","Charjabug","Crabominable","Ribombee","Lycanroc","Toxapex","Mudsdale","Araquanid","Lurantis","Shiinotic","Salazzle","Bewear","Steenee","Golisopod","Palossand","Jangmo O"]},{"weight":1024,"pokemon":["Bulbasaur","Charmander","Squirtle","Farfetch’d","Kangaskhan","Pinsir","Tauros","Lapras","Ditto","Aerodactyl","Chikorita","Cyndaquil","Totodile","Unown","Girafarig","Dunsparce","Qwilfish","Shuckle","Heracross","Corsola","Delibird","Skarmory","Stantler","Smeargle","Miltank","Treecko","Torchic","Mudkip","Sableye","Mawile","Plusle","Minun","Volbeat","Illumise","Torkoal","Spinda","Zangoose","Seviper","Lunatone","Solrock","Castform","Kecleon","Tropius","Absol","Relicanth","Luvdisc","Turtwig","Chimchar","Piplup","Pachirisu","Chatot","Spiritomb","Carnivine","Rotom","Snivy","Tepig","Oshawott","Audino","Throh","Sawk","Basculin","Maractus","Sigilyph","Emolga","Alomomola","Cryogonal","Stunfisk","Druddigon","Bouffalant","Heatmor","Durant","Chespin","Fennekin","Froakie","Furfrou","Hawlucha","Dedenne","Carbink","Klefki","Rowlet","Litten","Popplio","Oricorio","Wishiwashi","Comfey","Oranguru","Passimian","Pyukumuku","Minior","Komala","Turtonator","Togedemaru","Mimikyu","Bruxish","Drampa","Dhelmise"]},{"weight":1536,"pokemon":["Caterpie","Weedle","Pidgey","Rattata","Spearow","Ekans","Sandshrew","Nidoran F","Nidoran M","Vulpix","Zubat","Oddish","Paras","Venonat","Diglett","Meowth","Psyduck","Mankey","Growlithe","Poliwag","Abra","Machop","Bellsprout","Tentacool","Geodude","Ponyta","Slowpoke","Mangemite","Doduo","Seel","Grimer","Shellder","Gastly","Onix","Drowzee","Krabby","Voltorb","Exeggcute","Cubone","Lickitung","Koffing","Rhyhorn","Tangela","Horsea","Goldeen","Staryu","Scyther","Magikarp","Eevee","Porygon","Omanyte","Kabuto","Sentret","Hoothoot","Ledyba","Spinarak","Chinchou","Natu","Mareep","Hoppip","Aipom","Sunkern","Yanma","Wooper","Murkrow","Misdreavus","Pineco","Gligar","Snubbull","Sneasel","Teddiursa","Slugma","Swinub","Remoraid","Houndour","Phanpy","Poochyena","Zigzagoon","Wurmple","Lotad","Seedot","Taillow","Wingull","Ralts","Surskit","Shroomish","Slakoth","Nincada","Whismur","Makuhita","Nosepass","Skitty","Meditite","Electrike","Gulpin","Carvanha","Wailmer","Numel","Spoink","Trapinch","Cacnea","Swablu","Barboach","Corphish","Baltoy","Lileep","Anorith","Feebas","Shuppet","Duskull","Snorunt","Spheal","Clamperl","Starly","Bidoof","Kricketot","Shinx","Cranidos","Shieldon","Burmy","Combee","Buizel","Cherubi","Shellos","Drifloon","Buneary","Glameow","Stunky","Bronzor","Hippopotas","Skorupi","Croagunk","Finneon","Snover","Patrat","Lillipup","Purrloin","Pansage","Pansear","Panpour","Munna","Pidove","Blitzle","Roggenrola","Woobat","Drilbur","Timburr","Tympole","Sewaddle","Venipede","Cottonee","Petilil","Sandile","Darumaka","Dwebble","Scraggy","Yamask","Tirtouga","Archen","Trubbish","Zorua","Minccino","Gothita","Solosis","Ducklett","Vanillite","Deerling","Karrablast","Foongus","Frillish","Joltik","Ferroseed","Klink","Tynamo","Elgyem","Litwick","Axew","Cubchoo","Shelmet","Mienfoo","Golett","Pawniard","Rufflet","Vullaby","Larvesta","Bunnelby","Fletchling","Scatterbug","Litleo","Flabébé","Skiddo","Pancham","Espurr","Honedge","Spritzee","Swirlix","Inkay","Binacle","Skrelp","Clauncher","Helioptile","Tyrunt","Amaura","Phantump","Pumpkaboo","Bergmite","Noibat","Pikipek","Yungoos","Grubbin","Crabrawler","Cutiefly","Rockruff","Mareanie","Mudbray","Dewpider","Fomantis","Morelull","Salandit","Stufful","Bounsweet","Wimpod","Sandygast"]}]
//    """.trimIndent()
//
//    val pokemonSpawnWeights: List<PokemonSpawnWeight> = Json.decodeFromString(jsonStr)
//
//    val others = mutableListOf<PokemonSpawnWeightF>()
//
//    for (x in pokemonSpawnWeights) {
//      for (pName in x.pokemon) {
//        val p = Pokemon.getByName(pName) ?: continue
//        others.add(PokemonSpawnWeightF(p.id, x.weight))
//      }
//    }
//
//    others.sortBy { it.id }
//
//    println(Json.encodeToString(others))

    val jsonStr = """
      [{"id":1,"spawnWeight":1024},{"id":2,"spawnWeight":384},{"id":3,"spawnWeight":96},{"id":4,"spawnWeight":1024},{"id":5,"spawnWeight":384},{"id":6,"spawnWeight":96},{"id":7,"spawnWeight":1024},{"id":8,"spawnWeight":384},{"id":9,"spawnWeight":96},{"id":10,"spawnWeight":1536},{"id":11,"spawnWeight":512},{"id":12,"spawnWeight":256},{"id":13,"spawnWeight":1536},{"id":14,"spawnWeight":512},{"id":15,"spawnWeight":256},{"id":16,"spawnWeight":1536},{"id":17,"spawnWeight":512},{"id":18,"spawnWeight":256},{"id":19,"spawnWeight":1536},{"id":20,"spawnWeight":512},{"id":21,"spawnWeight":1536},{"id":22,"spawnWeight":512},{"id":23,"spawnWeight":1536},{"id":24,"spawnWeight":512},{"id":25,"spawnWeight":512},{"id":26,"spawnWeight":256},{"id":27,"spawnWeight":1536},{"id":28,"spawnWeight":512},{"id":29,"spawnWeight":1536},{"id":30,"spawnWeight":512},{"id":31,"spawnWeight":256},{"id":32,"spawnWeight":1536},{"id":33,"spawnWeight":512},{"id":34,"spawnWeight":256},{"id":35,"spawnWeight":512},{"id":36,"spawnWeight":256},{"id":37,"spawnWeight":1536},{"id":38,"spawnWeight":512},{"id":39,"spawnWeight":512},{"id":40,"spawnWeight":256},{"id":41,"spawnWeight":1536},{"id":42,"spawnWeight":512},{"id":43,"spawnWeight":1536},{"id":44,"spawnWeight":512},{"id":45,"spawnWeight":256},{"id":46,"spawnWeight":1536},{"id":47,"spawnWeight":512},{"id":48,"spawnWeight":1536},{"id":49,"spawnWeight":512},{"id":50,"spawnWeight":1536},{"id":51,"spawnWeight":512},{"id":52,"spawnWeight":1536},{"id":53,"spawnWeight":512},{"id":54,"spawnWeight":1536},{"id":55,"spawnWeight":512},{"id":56,"spawnWeight":1536},{"id":57,"spawnWeight":512},{"id":58,"spawnWeight":1536},{"id":59,"spawnWeight":512},{"id":60,"spawnWeight":1536},{"id":61,"spawnWeight":512},{"id":62,"spawnWeight":256},{"id":63,"spawnWeight":1536},{"id":64,"spawnWeight":512},{"id":65,"spawnWeight":256},{"id":66,"spawnWeight":1536},{"id":67,"spawnWeight":512},{"id":68,"spawnWeight":256},{"id":69,"spawnWeight":1536},{"id":70,"spawnWeight":512},{"id":71,"spawnWeight":256},{"id":72,"spawnWeight":1536},{"id":73,"spawnWeight":512},{"id":74,"spawnWeight":1536},{"id":75,"spawnWeight":512},{"id":76,"spawnWeight":256},{"id":77,"spawnWeight":1536},{"id":78,"spawnWeight":512},{"id":79,"spawnWeight":1536},{"id":80,"spawnWeight":512},{"id":83,"spawnWeight":1024},{"id":84,"spawnWeight":1536},{"id":85,"spawnWeight":512},{"id":86,"spawnWeight":1536},{"id":87,"spawnWeight":512},{"id":88,"spawnWeight":1536},{"id":89,"spawnWeight":512},{"id":90,"spawnWeight":1536},{"id":91,"spawnWeight":512},{"id":92,"spawnWeight":1536},{"id":93,"spawnWeight":512},{"id":94,"spawnWeight":256},{"id":95,"spawnWeight":1536},{"id":96,"spawnWeight":1536},{"id":97,"spawnWeight":512},{"id":98,"spawnWeight":1536},{"id":99,"spawnWeight":512},{"id":100,"spawnWeight":1536},{"id":101,"spawnWeight":512},{"id":102,"spawnWeight":1536},{"id":103,"spawnWeight":512},{"id":104,"spawnWeight":1536},{"id":105,"spawnWeight":512},{"id":106,"spawnWeight":512},{"id":107,"spawnWeight":512},{"id":108,"spawnWeight":1536},{"id":109,"spawnWeight":1536},{"id":110,"spawnWeight":512},{"id":111,"spawnWeight":1536},{"id":112,"spawnWeight":512},{"id":113,"spawnWeight":512},{"id":114,"spawnWeight":1536},{"id":115,"spawnWeight":1024},{"id":116,"spawnWeight":1536},{"id":117,"spawnWeight":512},{"id":118,"spawnWeight":1536},{"id":119,"spawnWeight":512},{"id":120,"spawnWeight":1536},{"id":121,"spawnWeight":512},{"id":122,"spawnWeight":512},{"id":123,"spawnWeight":1536},{"id":124,"spawnWeight":512},{"id":125,"spawnWeight":512},{"id":126,"spawnWeight":512},{"id":127,"spawnWeight":1024},{"id":128,"spawnWeight":1024},{"id":129,"spawnWeight":1536},{"id":130,"spawnWeight":512},{"id":131,"spawnWeight":1024},{"id":132,"spawnWeight":1024},{"id":133,"spawnWeight":1536},{"id":134,"spawnWeight":512},{"id":135,"spawnWeight":512},{"id":136,"spawnWeight":512},{"id":137,"spawnWeight":1536},{"id":138,"spawnWeight":1536},{"id":139,"spawnWeight":512},{"id":140,"spawnWeight":1536},{"id":141,"spawnWeight":512},{"id":142,"spawnWeight":1024},{"id":143,"spawnWeight":512},{"id":144,"spawnWeight":6},{"id":145,"spawnWeight":6},{"id":146,"spawnWeight":6},{"id":147,"spawnWeight":512},{"id":148,"spawnWeight":256},{"id":149,"spawnWeight":64},{"id":150,"spawnWeight":6},{"id":151,"spawnWeight":3},{"id":152,"spawnWeight":1024},{"id":153,"spawnWeight":384},{"id":154,"spawnWeight":96},{"id":155,"spawnWeight":1024},{"id":156,"spawnWeight":384},{"id":157,"spawnWeight":96},{"id":158,"spawnWeight":1024},{"id":159,"spawnWeight":384},{"id":160,"spawnWeight":96},{"id":161,"spawnWeight":1536},{"id":162,"spawnWeight":512},{"id":163,"spawnWeight":1536},{"id":164,"spawnWeight":512},{"id":165,"spawnWeight":1536},{"id":166,"spawnWeight":512},{"id":167,"spawnWeight":1536},{"id":168,"spawnWeight":512},{"id":169,"spawnWeight":256},{"id":170,"spawnWeight":1536},{"id":171,"spawnWeight":512},{"id":172,"spawnWeight":384},{"id":173,"spawnWeight":384},{"id":174,"spawnWeight":384},{"id":175,"spawnWeight":384},{"id":176,"spawnWeight":512},{"id":177,"spawnWeight":1536},{"id":178,"spawnWeight":512},{"id":179,"spawnWeight":1536},{"id":180,"spawnWeight":512},{"id":181,"spawnWeight":256},{"id":182,"spawnWeight":256},{"id":183,"spawnWeight":512},{"id":184,"spawnWeight":256},{"id":185,"spawnWeight":512},{"id":186,"spawnWeight":256},{"id":187,"spawnWeight":1536},{"id":188,"spawnWeight":512},{"id":189,"spawnWeight":256},{"id":190,"spawnWeight":1536},{"id":191,"spawnWeight":1536},{"id":192,"spawnWeight":512},{"id":193,"spawnWeight":1536},{"id":194,"spawnWeight":1536},{"id":195,"spawnWeight":512},{"id":196,"spawnWeight":512},{"id":197,"spawnWeight":512},{"id":198,"spawnWeight":1536},{"id":199,"spawnWeight":512},{"id":200,"spawnWeight":1536},{"id":201,"spawnWeight":1024},{"id":202,"spawnWeight":512},{"id":203,"spawnWeight":1024},{"id":204,"spawnWeight":1536},{"id":205,"spawnWeight":512},{"id":206,"spawnWeight":1024},{"id":207,"spawnWeight":1536},{"id":208,"spawnWeight":512},{"id":209,"spawnWeight":1536},{"id":210,"spawnWeight":512},{"id":211,"spawnWeight":1024},{"id":212,"spawnWeight":512},{"id":213,"spawnWeight":1024},{"id":214,"spawnWeight":1024},{"id":215,"spawnWeight":1536},{"id":216,"spawnWeight":1536},{"id":217,"spawnWeight":512},{"id":218,"spawnWeight":1536},{"id":219,"spawnWeight":512},{"id":220,"spawnWeight":1536},{"id":221,"spawnWeight":512},{"id":222,"spawnWeight":1024},{"id":223,"spawnWeight":1536},{"id":224,"spawnWeight":512},{"id":225,"spawnWeight":1024},{"id":226,"spawnWeight":512},{"id":227,"spawnWeight":1024},{"id":228,"spawnWeight":1536},{"id":229,"spawnWeight":512},{"id":230,"spawnWeight":256},{"id":231,"spawnWeight":1536},{"id":232,"spawnWeight":512},{"id":233,"spawnWeight":512},{"id":234,"spawnWeight":1024},{"id":235,"spawnWeight":1024},{"id":236,"spawnWeight":384},{"id":237,"spawnWeight":512},{"id":238,"spawnWeight":384},{"id":239,"spawnWeight":384},{"id":240,"spawnWeight":384},{"id":241,"spawnWeight":1024},{"id":242,"spawnWeight":256},{"id":243,"spawnWeight":6},{"id":244,"spawnWeight":6},{"id":245,"spawnWeight":6},{"id":246,"spawnWeight":512},{"id":247,"spawnWeight":256},{"id":248,"spawnWeight":64},{"id":249,"spawnWeight":6},{"id":250,"spawnWeight":6},{"id":251,"spawnWeight":3},{"id":252,"spawnWeight":1024},{"id":253,"spawnWeight":384},{"id":254,"spawnWeight":96},{"id":255,"spawnWeight":1024},{"id":256,"spawnWeight":384},{"id":257,"spawnWeight":96},{"id":258,"spawnWeight":1024},{"id":259,"spawnWeight":384},{"id":260,"spawnWeight":96},{"id":261,"spawnWeight":1536},{"id":262,"spawnWeight":512},{"id":263,"spawnWeight":1536},{"id":264,"spawnWeight":512},{"id":265,"spawnWeight":1536},{"id":266,"spawnWeight":512},{"id":267,"spawnWeight":256},{"id":268,"spawnWeight":512},{"id":269,"spawnWeight":256},{"id":270,"spawnWeight":1536},{"id":271,"spawnWeight":512},{"id":272,"spawnWeight":256},{"id":273,"spawnWeight":1536},{"id":274,"spawnWeight":512},{"id":275,"spawnWeight":256},{"id":276,"spawnWeight":1536},{"id":277,"spawnWeight":512},{"id":278,"spawnWeight":1536},{"id":279,"spawnWeight":512},{"id":280,"spawnWeight":1536},{"id":281,"spawnWeight":512},{"id":282,"spawnWeight":256},{"id":283,"spawnWeight":1536},{"id":284,"spawnWeight":512},{"id":285,"spawnWeight":1536},{"id":286,"spawnWeight":512},{"id":287,"spawnWeight":1536},{"id":288,"spawnWeight":512},{"id":289,"spawnWeight":256},{"id":290,"spawnWeight":1536},{"id":291,"spawnWeight":512},{"id":292,"spawnWeight":512},{"id":293,"spawnWeight":1536},{"id":294,"spawnWeight":512},{"id":295,"spawnWeight":256},{"id":296,"spawnWeight":1536},{"id":297,"spawnWeight":512},{"id":298,"spawnWeight":384},{"id":299,"spawnWeight":1536},{"id":300,"spawnWeight":1536},{"id":301,"spawnWeight":512},{"id":302,"spawnWeight":1024},{"id":303,"spawnWeight":1024},{"id":304,"spawnWeight":512},{"id":305,"spawnWeight":256},{"id":306,"spawnWeight":64},{"id":307,"spawnWeight":1536},{"id":308,"spawnWeight":512},{"id":309,"spawnWeight":1536},{"id":310,"spawnWeight":512},{"id":311,"spawnWeight":1024},{"id":312,"spawnWeight":1024},{"id":313,"spawnWeight":1024},{"id":314,"spawnWeight":1024},{"id":315,"spawnWeight":512},{"id":316,"spawnWeight":1536},{"id":317,"spawnWeight":512},{"id":318,"spawnWeight":1536},{"id":319,"spawnWeight":512},{"id":320,"spawnWeight":1536},{"id":321,"spawnWeight":512},{"id":322,"spawnWeight":1536},{"id":323,"spawnWeight":512},{"id":324,"spawnWeight":1024},{"id":325,"spawnWeight":1536},{"id":326,"spawnWeight":512},{"id":327,"spawnWeight":1024},{"id":328,"spawnWeight":1536},{"id":329,"spawnWeight":512},{"id":330,"spawnWeight":256},{"id":331,"spawnWeight":1536},{"id":332,"spawnWeight":512},{"id":333,"spawnWeight":1536},{"id":334,"spawnWeight":512},{"id":335,"spawnWeight":1024},{"id":336,"spawnWeight":1024},{"id":337,"spawnWeight":1024},{"id":338,"spawnWeight":1024},{"id":339,"spawnWeight":1536},{"id":340,"spawnWeight":512},{"id":341,"spawnWeight":1536},{"id":342,"spawnWeight":512},{"id":343,"spawnWeight":1536},{"id":344,"spawnWeight":512},{"id":345,"spawnWeight":1536},{"id":346,"spawnWeight":512},{"id":347,"spawnWeight":1536},{"id":348,"spawnWeight":512},{"id":349,"spawnWeight":1536},{"id":350,"spawnWeight":512},{"id":351,"spawnWeight":1024},{"id":352,"spawnWeight":1024},{"id":353,"spawnWeight":1536},{"id":354,"spawnWeight":512},{"id":355,"spawnWeight":1536},{"id":356,"spawnWeight":512},{"id":357,"spawnWeight":1024},{"id":358,"spawnWeight":256},{"id":359,"spawnWeight":1024},{"id":360,"spawnWeight":384},{"id":361,"spawnWeight":1536},{"id":362,"spawnWeight":512},{"id":363,"spawnWeight":1536},{"id":364,"spawnWeight":512},{"id":365,"spawnWeight":256},{"id":366,"spawnWeight":1536},{"id":367,"spawnWeight":512},{"id":368,"spawnWeight":512},{"id":369,"spawnWeight":1024},{"id":370,"spawnWeight":1024},{"id":371,"spawnWeight":512},{"id":372,"spawnWeight":256},{"id":373,"spawnWeight":64},{"id":374,"spawnWeight":512},{"id":375,"spawnWeight":256},{"id":376,"spawnWeight":64},{"id":377,"spawnWeight":6},{"id":378,"spawnWeight":6},{"id":379,"spawnWeight":6},{"id":380,"spawnWeight":6},{"id":381,"spawnWeight":6},{"id":382,"spawnWeight":6},{"id":383,"spawnWeight":6},{"id":384,"spawnWeight":6},{"id":385,"spawnWeight":3},{"id":386,"spawnWeight":3},{"id":387,"spawnWeight":1024},{"id":388,"spawnWeight":384},{"id":389,"spawnWeight":96},{"id":390,"spawnWeight":1024},{"id":391,"spawnWeight":384},{"id":392,"spawnWeight":96},{"id":393,"spawnWeight":1024},{"id":394,"spawnWeight":384},{"id":395,"spawnWeight":96},{"id":396,"spawnWeight":1536},{"id":397,"spawnWeight":512},{"id":398,"spawnWeight":256},{"id":399,"spawnWeight":1536},{"id":400,"spawnWeight":512},{"id":401,"spawnWeight":1536},{"id":402,"spawnWeight":512},{"id":403,"spawnWeight":1536},{"id":404,"spawnWeight":512},{"id":405,"spawnWeight":256},{"id":406,"spawnWeight":384},{"id":407,"spawnWeight":256},{"id":408,"spawnWeight":1536},{"id":409,"spawnWeight":512},{"id":410,"spawnWeight":1536},{"id":411,"spawnWeight":512},{"id":412,"spawnWeight":1536},{"id":413,"spawnWeight":512},{"id":414,"spawnWeight":512},{"id":415,"spawnWeight":1536},{"id":416,"spawnWeight":512},{"id":417,"spawnWeight":1024},{"id":418,"spawnWeight":1536},{"id":419,"spawnWeight":512},{"id":420,"spawnWeight":1536},{"id":421,"spawnWeight":512},{"id":422,"spawnWeight":1536},{"id":423,"spawnWeight":512},{"id":424,"spawnWeight":512},{"id":425,"spawnWeight":1536},{"id":426,"spawnWeight":512},{"id":427,"spawnWeight":1536},{"id":428,"spawnWeight":512},{"id":429,"spawnWeight":512},{"id":430,"spawnWeight":512},{"id":431,"spawnWeight":1536},{"id":432,"spawnWeight":512},{"id":433,"spawnWeight":384},{"id":434,"spawnWeight":1536},{"id":435,"spawnWeight":512},{"id":436,"spawnWeight":1536},{"id":437,"spawnWeight":512},{"id":438,"spawnWeight":384},{"id":439,"spawnWeight":384},{"id":440,"spawnWeight":384},{"id":441,"spawnWeight":1024},{"id":442,"spawnWeight":1024},{"id":443,"spawnWeight":512},{"id":444,"spawnWeight":256},{"id":445,"spawnWeight":64},{"id":446,"spawnWeight":384},{"id":447,"spawnWeight":384},{"id":448,"spawnWeight":512},{"id":449,"spawnWeight":1536},{"id":450,"spawnWeight":512},{"id":451,"spawnWeight":1536},{"id":452,"spawnWeight":512},{"id":453,"spawnWeight":1536},{"id":454,"spawnWeight":512},{"id":455,"spawnWeight":1024},{"id":456,"spawnWeight":1536},{"id":457,"spawnWeight":512},{"id":458,"spawnWeight":384},{"id":459,"spawnWeight":1536},{"id":460,"spawnWeight":512},{"id":461,"spawnWeight":512},{"id":462,"spawnWeight":256},{"id":463,"spawnWeight":512},{"id":464,"spawnWeight":256},{"id":465,"spawnWeight":512},{"id":466,"spawnWeight":256},{"id":467,"spawnWeight":256},{"id":468,"spawnWeight":256},{"id":469,"spawnWeight":512},{"id":470,"spawnWeight":512},{"id":471,"spawnWeight":512},{"id":472,"spawnWeight":512},{"id":473,"spawnWeight":256},{"id":474,"spawnWeight":256},{"id":475,"spawnWeight":256},{"id":476,"spawnWeight":512},{"id":477,"spawnWeight":256},{"id":478,"spawnWeight":512},{"id":479,"spawnWeight":1024},{"id":480,"spawnWeight":6},{"id":481,"spawnWeight":6},{"id":482,"spawnWeight":6},{"id":483,"spawnWeight":6},{"id":484,"spawnWeight":6},{"id":485,"spawnWeight":6},{"id":486,"spawnWeight":6},{"id":487,"spawnWeight":6},{"id":488,"spawnWeight":6},{"id":489,"spawnWeight":3},{"id":490,"spawnWeight":3},{"id":491,"spawnWeight":3},{"id":492,"spawnWeight":3},{"id":493,"spawnWeight":3},{"id":494,"spawnWeight":3},{"id":495,"spawnWeight":1024},{"id":496,"spawnWeight":384},{"id":497,"spawnWeight":96},{"id":498,"spawnWeight":1024},{"id":499,"spawnWeight":384},{"id":500,"spawnWeight":96},{"id":501,"spawnWeight":1024},{"id":502,"spawnWeight":384},{"id":503,"spawnWeight":96},{"id":504,"spawnWeight":1536},{"id":505,"spawnWeight":512},{"id":506,"spawnWeight":1536},{"id":507,"spawnWeight":512},{"id":508,"spawnWeight":256},{"id":509,"spawnWeight":1536},{"id":510,"spawnWeight":512},{"id":511,"spawnWeight":1536},{"id":512,"spawnWeight":512},{"id":513,"spawnWeight":1536},{"id":514,"spawnWeight":512},{"id":515,"spawnWeight":1536},{"id":516,"spawnWeight":512},{"id":517,"spawnWeight":1536},{"id":518,"spawnWeight":512},{"id":519,"spawnWeight":1536},{"id":520,"spawnWeight":512},{"id":521,"spawnWeight":256},{"id":522,"spawnWeight":1536},{"id":523,"spawnWeight":512},{"id":524,"spawnWeight":1536},{"id":525,"spawnWeight":512},{"id":526,"spawnWeight":256},{"id":527,"spawnWeight":1536},{"id":528,"spawnWeight":512},{"id":529,"spawnWeight":1536},{"id":530,"spawnWeight":512},{"id":531,"spawnWeight":1024},{"id":532,"spawnWeight":1536},{"id":533,"spawnWeight":512},{"id":534,"spawnWeight":256},{"id":535,"spawnWeight":1536},{"id":536,"spawnWeight":512},{"id":537,"spawnWeight":256},{"id":538,"spawnWeight":1024},{"id":539,"spawnWeight":1024},{"id":540,"spawnWeight":1536},{"id":541,"spawnWeight":512},{"id":542,"spawnWeight":256},{"id":543,"spawnWeight":1536},{"id":544,"spawnWeight":512},{"id":545,"spawnWeight":256},{"id":546,"spawnWeight":1536},{"id":547,"spawnWeight":512},{"id":548,"spawnWeight":1536},{"id":549,"spawnWeight":512},{"id":550,"spawnWeight":1024},{"id":551,"spawnWeight":1536},{"id":552,"spawnWeight":512},{"id":553,"spawnWeight":256},{"id":554,"spawnWeight":1536},{"id":555,"spawnWeight":512},{"id":556,"spawnWeight":1024},{"id":557,"spawnWeight":1536},{"id":558,"spawnWeight":512},{"id":559,"spawnWeight":1536},{"id":560,"spawnWeight":512},{"id":561,"spawnWeight":1024},{"id":562,"spawnWeight":1536},{"id":563,"spawnWeight":512},{"id":564,"spawnWeight":1536},{"id":565,"spawnWeight":512},{"id":566,"spawnWeight":1536},{"id":567,"spawnWeight":512},{"id":568,"spawnWeight":1536},{"id":569,"spawnWeight":512},{"id":570,"spawnWeight":1536},{"id":571,"spawnWeight":512},{"id":572,"spawnWeight":1536},{"id":573,"spawnWeight":512},{"id":574,"spawnWeight":1536},{"id":575,"spawnWeight":512},{"id":576,"spawnWeight":256},{"id":577,"spawnWeight":1536},{"id":578,"spawnWeight":512},{"id":579,"spawnWeight":256},{"id":580,"spawnWeight":1536},{"id":581,"spawnWeight":512},{"id":582,"spawnWeight":1536},{"id":583,"spawnWeight":512},{"id":584,"spawnWeight":256},{"id":585,"spawnWeight":1536},{"id":586,"spawnWeight":512},{"id":587,"spawnWeight":1024},{"id":588,"spawnWeight":1536},{"id":589,"spawnWeight":512},{"id":590,"spawnWeight":1536},{"id":591,"spawnWeight":512},{"id":592,"spawnWeight":1536},{"id":593,"spawnWeight":512},{"id":594,"spawnWeight":1024},{"id":595,"spawnWeight":1536},{"id":596,"spawnWeight":512},{"id":597,"spawnWeight":1536},{"id":598,"spawnWeight":512},{"id":599,"spawnWeight":1536},{"id":600,"spawnWeight":512},{"id":601,"spawnWeight":256},{"id":602,"spawnWeight":1536},{"id":603,"spawnWeight":512},{"id":604,"spawnWeight":256},{"id":605,"spawnWeight":1536},{"id":606,"spawnWeight":512},{"id":607,"spawnWeight":1536},{"id":608,"spawnWeight":512},{"id":609,"spawnWeight":256},{"id":610,"spawnWeight":1536},{"id":611,"spawnWeight":512},{"id":612,"spawnWeight":256},{"id":613,"spawnWeight":1536},{"id":614,"spawnWeight":512},{"id":615,"spawnWeight":1024},{"id":616,"spawnWeight":1536},{"id":617,"spawnWeight":512},{"id":618,"spawnWeight":1024},{"id":619,"spawnWeight":1536},{"id":620,"spawnWeight":512},{"id":621,"spawnWeight":1024},{"id":622,"spawnWeight":1536},{"id":623,"spawnWeight":512},{"id":624,"spawnWeight":1536},{"id":625,"spawnWeight":512},{"id":626,"spawnWeight":1024},{"id":627,"spawnWeight":1536},{"id":628,"spawnWeight":512},{"id":629,"spawnWeight":1536},{"id":630,"spawnWeight":512},{"id":631,"spawnWeight":1024},{"id":632,"spawnWeight":1024},{"id":633,"spawnWeight":512},{"id":634,"spawnWeight":256},{"id":635,"spawnWeight":64},{"id":636,"spawnWeight":1536},{"id":637,"spawnWeight":512},{"id":638,"spawnWeight":6},{"id":639,"spawnWeight":6},{"id":640,"spawnWeight":6},{"id":641,"spawnWeight":6},{"id":642,"spawnWeight":6},{"id":643,"spawnWeight":6},{"id":644,"spawnWeight":6},{"id":645,"spawnWeight":6},{"id":646,"spawnWeight":6},{"id":647,"spawnWeight":3},{"id":648,"spawnWeight":3},{"id":649,"spawnWeight":3},{"id":650,"spawnWeight":1024},{"id":651,"spawnWeight":384},{"id":652,"spawnWeight":96},{"id":653,"spawnWeight":1024},{"id":654,"spawnWeight":384},{"id":655,"spawnWeight":96},{"id":656,"spawnWeight":1024},{"id":657,"spawnWeight":384},{"id":658,"spawnWeight":96},{"id":659,"spawnWeight":1536},{"id":660,"spawnWeight":512},{"id":661,"spawnWeight":1536},{"id":662,"spawnWeight":512},{"id":663,"spawnWeight":256},{"id":664,"spawnWeight":1536},{"id":665,"spawnWeight":512},{"id":666,"spawnWeight":256},{"id":667,"spawnWeight":1536},{"id":668,"spawnWeight":512},{"id":669,"spawnWeight":1536},{"id":670,"spawnWeight":512},{"id":671,"spawnWeight":256},{"id":672,"spawnWeight":1536},{"id":673,"spawnWeight":512},{"id":674,"spawnWeight":1536},{"id":675,"spawnWeight":512},{"id":676,"spawnWeight":1024},{"id":677,"spawnWeight":1536},{"id":678,"spawnWeight":512},{"id":679,"spawnWeight":1536},{"id":680,"spawnWeight":512},{"id":681,"spawnWeight":256},{"id":682,"spawnWeight":1536},{"id":683,"spawnWeight":512},{"id":684,"spawnWeight":1536},{"id":685,"spawnWeight":512},{"id":686,"spawnWeight":1536},{"id":687,"spawnWeight":512},{"id":688,"spawnWeight":1536},{"id":689,"spawnWeight":512},{"id":690,"spawnWeight":1536},{"id":691,"spawnWeight":512},{"id":692,"spawnWeight":1536},{"id":693,"spawnWeight":512},{"id":694,"spawnWeight":1536},{"id":695,"spawnWeight":512},{"id":696,"spawnWeight":1536},{"id":697,"spawnWeight":512},{"id":698,"spawnWeight":1536},{"id":699,"spawnWeight":512},{"id":700,"spawnWeight":512},{"id":701,"spawnWeight":1024},{"id":702,"spawnWeight":1024},{"id":703,"spawnWeight":1024},{"id":704,"spawnWeight":512},{"id":705,"spawnWeight":256},{"id":706,"spawnWeight":64},{"id":707,"spawnWeight":1024},{"id":708,"spawnWeight":1536},{"id":709,"spawnWeight":512},{"id":710,"spawnWeight":1536},{"id":711,"spawnWeight":512},{"id":712,"spawnWeight":1536},{"id":713,"spawnWeight":512},{"id":714,"spawnWeight":1536},{"id":715,"spawnWeight":512},{"id":716,"spawnWeight":6},{"id":717,"spawnWeight":6},{"id":718,"spawnWeight":6},{"id":719,"spawnWeight":3},{"id":720,"spawnWeight":3},{"id":721,"spawnWeight":3},{"id":722,"spawnWeight":1024},{"id":723,"spawnWeight":384},{"id":724,"spawnWeight":96},{"id":725,"spawnWeight":1024},{"id":726,"spawnWeight":384},{"id":727,"spawnWeight":96},{"id":728,"spawnWeight":1024},{"id":729,"spawnWeight":384},{"id":730,"spawnWeight":96},{"id":731,"spawnWeight":1536},{"id":732,"spawnWeight":512},{"id":733,"spawnWeight":256},{"id":734,"spawnWeight":1536},{"id":735,"spawnWeight":512},{"id":736,"spawnWeight":1536},{"id":737,"spawnWeight":512},{"id":738,"spawnWeight":256},{"id":739,"spawnWeight":1536},{"id":740,"spawnWeight":512},{"id":741,"spawnWeight":1024},{"id":742,"spawnWeight":1536},{"id":743,"spawnWeight":512},{"id":744,"spawnWeight":1536},{"id":745,"spawnWeight":512},{"id":746,"spawnWeight":1024},{"id":747,"spawnWeight":1536},{"id":748,"spawnWeight":512},{"id":749,"spawnWeight":1536},{"id":750,"spawnWeight":512},{"id":751,"spawnWeight":1536},{"id":752,"spawnWeight":512},{"id":753,"spawnWeight":1536},{"id":754,"spawnWeight":512},{"id":755,"spawnWeight":1536},{"id":756,"spawnWeight":512},{"id":757,"spawnWeight":1536},{"id":758,"spawnWeight":512},{"id":759,"spawnWeight":1536},{"id":760,"spawnWeight":512},{"id":761,"spawnWeight":1536},{"id":762,"spawnWeight":512},{"id":763,"spawnWeight":256},{"id":764,"spawnWeight":1024},{"id":765,"spawnWeight":1024},{"id":766,"spawnWeight":1024},{"id":767,"spawnWeight":1536},{"id":768,"spawnWeight":512},{"id":769,"spawnWeight":1536},{"id":770,"spawnWeight":512},{"id":771,"spawnWeight":1024},{"id":772,"spawnWeight":6},{"id":773,"spawnWeight":6},{"id":774,"spawnWeight":1024},{"id":775,"spawnWeight":1024},{"id":776,"spawnWeight":1024},{"id":777,"spawnWeight":1024},{"id":778,"spawnWeight":1024},{"id":779,"spawnWeight":1024},{"id":780,"spawnWeight":1024},{"id":781,"spawnWeight":1024},{"id":782,"spawnWeight":512},{"id":783,"spawnWeight":256},{"id":784,"spawnWeight":64},{"id":785,"spawnWeight":6},{"id":786,"spawnWeight":6},{"id":787,"spawnWeight":6},{"id":788,"spawnWeight":6},{"id":789,"spawnWeight":6},{"id":790,"spawnWeight":6},{"id":791,"spawnWeight":6},{"id":792,"spawnWeight":6},{"id":793,"spawnWeight":12},{"id":794,"spawnWeight":12},{"id":795,"spawnWeight":12},{"id":796,"spawnWeight":12},{"id":797,"spawnWeight":12},{"id":798,"spawnWeight":12},{"id":799,"spawnWeight":12},{"id":800,"spawnWeight":6},{"id":801,"spawnWeight":3},{"id":802,"spawnWeight":3},{"id":803,"spawnWeight":12},{"id":804,"spawnWeight":12},{"id":805,"spawnWeight":12},{"id":806,"spawnWeight":12},{"id":807,"spawnWeight":3}]
    """.trimIndent()

    var pokemonSpawnWeights: List<PokemonSpawnWeight> = Json.decodeFromString(jsonStr)

    pokemonSpawnWeights = pokemonSpawnWeights.map {
      if (it.spawnWeight in 3..96) it.spawnWeight = it.spawnWeight * 2
      it
    }

//    println(pokemonSpawnWeights)

    val x = pokemonSpawnWeights.sortedBy { it.spawnWeight }.groupBy {
      it.spawnWeight
    }

    for (entry in x) {
      println("${entry.key} - ${entry.value.joinToString(", ") { Pokemon.getById(it.id)!!.name }}")
    }

    val items = pokemonSpawnWeights.map { it.id }
    val weights = pokemonSpawnWeights.map { it.spawnWeight }

    val weightedRandomizer = WeightedRandomizer(items, weights)

    val legendary = mutableListOf<Int>()
    val mythical = mutableListOf<Int>()
    val ultraBeast = mutableListOf<Int>()
    val pseudoLegendary = mutableListOf<Int>()

    for (count in 0..100_000_000) {
      val randomId = weightedRandomizer.randomChoice()
      val pokemon = Pokemon.getById(randomId)!!
      println("$count. ${pokemon.name}")

      when {
        Pokemon.legendaries.contains(randomId) -> legendary.add(count)
        Pokemon.mythicals.contains(randomId) -> mythical.add(count)
        Pokemon.ultraBeasts.contains(randomId) -> ultraBeast.add(count)
        Pokemon.pseudoLegendaries.contains(randomId) -> pseudoLegendary.add(count)
      }

      if (legendary.isNotEmpty() && mythical.isNotEmpty() && ultraBeast.isNotEmpty() && pseudoLegendary.isNotEmpty()) break
    }

    println("Got ${legendary.size} legendary at $legendary")
    println("Got ${mythical.size} mythical at $mythical")
    println("Got ${ultraBeast.size} ultra-beast at $ultraBeast")
    println("Got ${pseudoLegendary.size} pseudo-legendary at $pseudoLegendary")
  }

  private fun simulateShiny() {
    var chance = 4908.0
    for (i in 0..100_000_000) {
      val rand = Random.nextDouble(chance)
      if (rand <= 1) {
        println("Got shiny at ${i + 1}, chance was 1 / $chance")
        break
      } else {
        chance -= 0.50
      }
    }
  }

  @JvmStatic
  fun main(args: Array<String>) {
    simulateSpawnWeight()
//    repeat(30) {
//      simulateShiny()
//    }

//    val newWeights = mapOf(
//      1536 to listOf(1)
//    )
  }
}
