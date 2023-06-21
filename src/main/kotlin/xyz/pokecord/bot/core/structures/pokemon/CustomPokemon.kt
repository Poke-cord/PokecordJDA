package xyz.pokecord.bot.core.structures.pokemon

object CustomPokemon {
  fun init() {
    //Import base Pokémon characteristics
    val charizard = Pokemon.getByName("Charizard")!!
    val pidgey = Pokemon.getByName("Pidgey")!!
    val pikachu = Pokemon.getByName("Pikachu")!!
    //Should be fine
    val nidoranF = Pokemon.getById(29)!!
    val venomoth = Pokemon.getByName("Venomoth")!!
    val mankey = Pokemon.getByName("Mankey")!!
    val chansey = Pokemon.getByName("Chansey")!!
    val eevee = Pokemon.getByName("Eevee")!!
    val mewtwo = Pokemon.getByName("Mewtwo")!!
    val umbreon = Pokemon.getByName("Umbreon")!!
    val linoone = Pokemon.getByName("linoone")!!
    val wingull = Pokemon.getByName("Wingull")!!
    val minun = Pokemon.getByName("Minun")!!
    val wailord = Pokemon.getByName("Wailord")!!
    val lunatone = Pokemon.getByName("Lunatone")!!
    val solrock = Pokemon.getByName("Solrock")!!
    val milotic = Pokemon.getByName("Milotic")!!
    val rayquaza = Pokemon.getByName("Rayquaza")!!
    val prinplup = Pokemon.getByName("Prinplup")!!
    val wormadam = Pokemon.getByName("Wormadam")!!
    val garchomp = Pokemon.getByName("Garchomp")!!
    val lucario = Pokemon.getByName("Lucario")!!
    val darkrai = Pokemon.getByName("Darkrai")!!
    val arceus = Pokemon.getByName("Arceus")!!
    val serperior = Pokemon.getByName("Seperior")!!
    val dewott = Pokemon.getByName("Dewott")!!
    val greninja = Pokemon.getByName("Greninja")!!
    val trevenant = Pokemon.getByName("Trevenant")!!
    val bergmite = Pokemon.getByName("Bergmite")!!
    val yveltal = Pokemon.getByName("Yveltal")!!
    val togedemaru = Pokemon.getByName("Togedemaru")!!
    val rookidee = Pokemon.getByName("Rookidee")!!
    val boltund = Pokemon.getByName("Boltund")!!
    val runerigus = Pokemon.getByName("Runerigus")!!
    val arctovish = Pokemon.getByName("Arctovish")!!

    //Create copy with some updated attributes
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
    val pridePidgeyV1 = pidgey.copy(
      id = 100000003,
      identifier = "pride-pidgey-v1",
      name = "Pride Pidgey V1",
      isDefault = false,
      formName = "Pride Pidgey V1",
    )
    val pridePidgeyV2 = pidgey.copy(
      id = 100000004,
      identifier = "pride-pidgey-v2",
      name = "Pride Pidgey V2",
      isDefault = false,
      formName = "Pride Pidgey V2",
    )
    val pridePikachuV1 = pikachu.copy(
      id = 100000005,
      identifier = "pride-pikachu-v1",
      name = "Pride Pikachu V1",
      isDefault = false,
      formName = "Pride Pikachu V1",
    )
    val pridePikachuV2 = pikachu.copy(
      id = 100000006,
      identifier = "pride-pikachu-v2",
      name = "Pride Pikachu V2",
      isDefault = false,
      formName = "Pride Pikachu V2",
    )
    val prideNidoranFV1 = nidoranF.copy(
      id = 100000007,
      identifier = "pride-nidoran♀-v1",
      name = "Pride Nidoran♀ V1",
      isDefault = false,
      formName = "Pride Nidoran♀ V1",
    )
    val prideNidoranFV2 = nidoranF.copy(
      id = 100000008,
      identifier = "pride-nidoran♀-v2",
      name = "Pride Nidoran♀ V2",
      isDefault = false,
      formName = "Pride Nidoran♀ V2",
    )
    val prideVenomothV1 = venomoth.copy(
      id = 100000009,
      identifier = "pride-venomoth-v1",
      name = "Pride Venomoth V1",
      isDefault = false,
      formName = "Pride Venomoth V1",
    )
    val prideVenomothV2 = venomoth.copy(
      id = 100000010,
      identifier = "pride-venomoth-v2",
      name = "Pride Venomoth V2",
      isDefault = false,
      formName = "Pride Venomoth V2",
    )
    val prideMankeyV1 = mankey.copy(
      id = 100000011,
      identifier = "pride-mankey-v1",
      name = "Pride Mankey V1",
      isDefault = false,
      formName = "Pride Mankey V1",
    )
    val prideMankeyV2 = mankey.copy(
      id = 100000012,
      identifier = "pride-mankey-v2",
      name = "Pride Mankey V2",
      isDefault = false,
      formName = "Pride Mankey V2",
    )
    val prideChanseyV1 = chansey.copy(
      id = 100000013,
      identifier = "pride-chansey-v1",
      name = "Pride Chansey V1",
      isDefault = false,
      formName = "Pride Chansey V1",
    )
    val prideChanseyV2 = chansey.copy(
      id = 100000014,
      identifier = "pride-chansey-v2",
      name = "Pride Chansey V2",
      isDefault = false,
      formName = "Pride Chansey V2",
    )
    val prideEeveeV1 = eevee.copy(
      id = 100000015,
      identifier = "pride-eevee-v1",
      name = "Pride Eevee V1",
      isDefault = false,
      formName = "Pride Eevee V1",
    )
    val prideEeveeV2 = eevee.copy(
      id = 100000016,
      identifier = "pride-eevee-v2",
      name = "Pride Eevee V2",
      isDefault = false,
      formName = "Pride Eevee V2",
    )
    val prideMewtwoV1 = mewtwo.copy(
      id = 100000017,
      identifier = "pride-mewtwo-v1",
      name = "Pride Mewtwo V1",
      isDefault = false,
      formName = "Pride Mewtwo V1",
    )
    val prideMewtwoV2 = mewtwo.copy(
      id = 100000018,
      identifier = "pride-mewtwo-v2",
      name = "Pride Mewtwo V2",
      isDefault = false,
      formName = "Pride Mewtwo V2",
    )
    val prideUmbreonV1 = umbreon.copy(
      id = 100000019,
      identifier = "pride-umbreon-v1",
      name = "Pride Umbreon V1",
      isDefault = false,
      formName = "Pride Umbreon V1",
    )
    val prideUmbreonV2 = umbreon.copy(
      id = 100000020,
      identifier = "pride-umbreon-v2",
      name = "Pride Umbreon V2",
      isDefault = false,
      formName = "Pride Umbreon V2",
    )
    val prideLinooneV1 = linoone.copy(
      id = 100000021,
      identifier = "pride-linoone-v1",
      name = "Pride Linoone V1",
      isDefault = false,
      formName = "Pride Linoone V1",
    )
    val prideLinooneV2 = linoone.copy(
      id = 100000022,
      identifier = "pride-linoone-v2",
      name = "Pride Linoone V2",
      isDefault = false,
      formName = "Pride Linoone V2",
    )
    val prideWingullV1 = wingull.copy(
      id = 100000023,
      identifier = "pride-wingull-v1",
      name = "Pride Wingull V1",
      isDefault = false,
      formName = "Pride Wingull V1",
    )
    val prideWingullV2 = wingull.copy(
      id = 100000024,
      identifier = "pride-wingull-v2",
      name = "Pride Wingull V2",
      isDefault = false,
      formName = "Pride Wingull V2",
    )
    val prideMinunV1 = minun.copy(
      id = 100000025,
      identifier = "pride-minun-v1",
      name = "Pride Minun V1",
      isDefault = false,
      formName = "Pride Minun V1",
    )
    val prideMinunV2 = minun.copy(
      id = 100000026,
      identifier = "pride-minun-v2",
      name = "Pride Minun V2",
      isDefault = false,
      formName = "Pride Minun V2",
    )
    val prideWailordV1 = wailord.copy(
      id = 100000027,
      identifier = "pride-wailord-v1",
      name = "Pride Wailord V1",
      isDefault = false,
      formName = "Pride Wailord V1",
    )
    val prideWailordV2 = wailord.copy(
      id = 100000028,
      identifier = "pride-wailord-v2",
      name = "Pride Wailord V2",
      isDefault = false,
      formName = "Pride Wailord V2",
    )
    val prideLunatoneV1 = lunatone.copy(
      id = 100000029,
      identifier = "pride-lunatone-v1",
      name = "Pride Lunatone V1",
      isDefault = false,
      formName = "Pride Lunatone V1",
    )
    val prideLunatoneV2 = lunatone.copy(
      id = 100000030,
      identifier = "pride-lunatone-v2",
      name = "Pride Lunatone V2",
      isDefault = false,
      formName = "Pride Lunatone V2",
    )
    val prideSolrockV1 = solrock.copy(
      id = 100000031,
      identifier = "pride-solrock-v1",
      name = "Pride Solrock V1",
      isDefault = false,
      formName = "Pride Solrock V1",
    )
    val prideSolrockV2 = solrock.copy(
      id = 100000032,
      identifier = "pride-solrock-v2",
      name = "Pride Solrock V2",
      isDefault = false,
      formName = "Pride Solrock V2",
    )
    val prideMiloticV1 = milotic.copy(
      id = 100000033,
      identifier = "pride-milotic-v1",
      name = "Pride Milotic V1",
      isDefault = false,
      formName = "Pride Milotic V1",
    )
    val prideMiloticV2 = milotic.copy(
      id = 100000034,
      identifier = "pride-milotic-v2",
      name = "Pride Milotic V2",
      isDefault = false,
      formName = "Pride Milotic V2",
    )
    val prideRayquazaV1 = rayquaza.copy(
      id = 100000035,
      identifier = "pride-rayquaza-v1",
      name = "Pride Rayquaza V1",
      isDefault = false,
      formName = "Pride Rayquaza V1",
    )
    val prideRayquazaV2 = rayquaza.copy(
      id = 100000036,
      identifier = "pride-rayquaza-v2",
      name = "Pride Rayquaza V2",
      isDefault = false,
      formName = "Pride Rayquaza V2",
    )
    val pridePrinplupV1 = prinplup.copy(
      id = 100000037,
      identifier = "pride-prinplup-v1",
      name = "Pride Prinplup V1",
      isDefault = false,
      formName = "Pride Prinplup V1",
    )
    val pridePrinplupV2 = prinplup.copy(
      id = 100000038,
      identifier = "pride-prinplup-v2",
      name = "Pride Prinplup V2",
      isDefault = false,
      formName = "Pride Prinplup V2",
    )
    val prideWormadamV1 = wormadam.copy(
      id = 100000039,
      identifier = "pride-wormadam-v1",
      name = "Pride Wormadam V1",
      isDefault = false,
      formName = "Pride Wormadam V1",
    )
    val prideWormadamV2 = wormadam.copy(
      id = 100000040,
      identifier = "pride-wormadam-v2",
      name = "Pride Wormadam V2",
      isDefault = false,
      formName = "Pride Wormadam V2",
    )
    val prideGarchompV1 = garchomp.copy(
      id = 100000041,
      identifier = "pride-garchomp-v1",
      name = "Pride Garchomp V1",
      isDefault = false,
      formName = "Pride Garchomp V1",
    )
    val prideGarchompV2 = garchomp.copy(
      id = 100000042,
      identifier = "pride-garchomp-v2",
      name = "Pride Garchomp V2",
      isDefault = false,
      formName = "Pride Garchomp V2",
    )
    val prideLucarioV1 = lucario.copy(
      id = 100000043,
      identifier = "pride-lucario-v1",
      name = "Pride Lucario V1",
      isDefault = false,
      formName = "Pride Lucario V1",
    )
    val prideLucarioV2 = lucario.copy(
      id = 100000044,
      identifier = "pride-lucario-v2",
      name = "Pride Lucario V2",
      isDefault = false,
      formName = "Pride Lucario V2",
    )
    val prideDarkraiV1 = darkrai.copy(
      id = 100000045,
      identifier = "pride-darkrai-v1",
      name = "Pride Darkrai V1",
      isDefault = false,
      formName = "Pride Darkrai V1",
    )
    val prideDarkraiV2 = darkrai.copy(
      id = 100000046,
      identifier = "pride-darkrai-v2",
      name = "Pride Darkrai V2",
      isDefault = false,
      formName = "Pride Darkrai V2",
    )
    val prideArceusV1 = arceus.copy(
      id = 100000047,
      identifier = "pride-arceus-v1",
      name = "Pride Arceus V1",
      isDefault = false,
      formName = "Pride Arceus V1",
    )
    val prideArceusV2 = arceus.copy(
      id = 100000048,
      identifier = "pride-arceus-v2",
      name = "Pride Arceus V2",
      isDefault = false,
      formName = "Pride Arceus V2",
    )
    val prideSerperiorV1 = serperior.copy(
      id = 100000049,
      identifier = "pride-serperior-v1",
      name = "Pride Serperior V1",
      isDefault = false,
      formName = "Pride Serperior V1",
    )
    val prideSerperiorV2 = serperior.copy(
      id = 100000050,
      identifier = "pride-serperior-v2",
      name = "Pride Serperior V2",
      isDefault = false,
      formName = "Pride Serperior V2",
    )
    val prideDewottV1 = dewott.copy(
      id = 100000051,
      identifier = "pride-dewott-v1",
      name = "Pride Dewott V1",
      isDefault = false,
      formName = "Pride Dewott V1",
    )
    val prideDewottV2 = dewott.copy(
      id = 100000052,
      identifier = "pride-dewott-v2",
      name = "Pride Dewott V2",
      isDefault = false,
      formName = "Pride Dewott V2",
    )
    val prideGreninjaV1 = greninja.copy(
      id = 100000053,
      identifier = "pride-greninja-v1",
      name = "Pride Greninja V1",
      isDefault = false,
      formName = "Pride Greninja V1",
    )
    val prideGreninjaV2 = greninja.copy(
      id = 100000054,
      identifier = "pride-greninja-v2",
      name = "Pride Greninja V2",
      isDefault = false,
      formName = "Pride Greninja V2",
    )
    val prideTrevenantV1 = trevenant.copy(
      id = 100000055,
      identifier = "pride-trevenant-v1",
      name = "Pride Trevenant V1",
      isDefault = false,
      formName = "Pride Trevenant V1",
    )
    val prideTrevenantV2 = trevenant.copy(
      id = 100000056,
      identifier = "pride-trevenant-v2",
      name = "Pride Trevenant V2",
      isDefault = false,
      formName = "Pride Trevenant V2",
    )
    val prideBergmiteV1 = bergmite.copy(
      id = 100000057,
      identifier = "pride-bergmite-v1",
      name = "Pride Bergmite V1",
      isDefault = false,
      formName = "Pride Bermite V1",
    )
    val prideBergmiteV2 = bergmite.copy(
      id = 100000058,
      identifier = "pride-bergmite-v2",
      name = "Pride Bergmite V2",
      isDefault = false,
      formName = "Pride Bermite V2",
    )
    val prideYveltalV1 = yveltal.copy(
      id = 100000059,
      identifier = "pride-yveltal-v1",
      name = "Pride Yveltal V1",
      isDefault = false,
      formName = "Pride Yveltal V1",
    )
    val prideYveltalV2 = yveltal.copy(
      id = 100000060,
      identifier = "pride-yveltal-v2",
      name = "Pride Yveltal V2",
      isDefault = false,
      formName = "Pride Yveltal V2",
    )
    val prideTogedemaruV1 = togedemaru.copy(
      id = 100000061,
      identifier = "pride-togedemaru-v1",
      name = "Pride Togedemaru V1",
      isDefault = false,
      formName = "Pride Togedemaru V1",
    )
    val prideTogedemaruV2 = togedemaru.copy(
      id = 100000062,
      identifier = "pride-togedemaru-v2",
      name = "Pride Togedemaru V2",
      isDefault = false,
      formName = "Pride Togedemaru V2",
    )
    val prideRookideeV1 = rookidee.copy(
      id = 100000063,
      identifier = "pride-rookidee-v1",
      name = "Pride Rookidee V1",
      isDefault = false,
      formName = "Pride Rookidee V1",
    )
    val prideRookideeV2 = rookidee.copy(
      id = 100000064,
      identifier = "pride-rookidee-v2",
      name = "Pride Rookidee V2",
      isDefault = false,
      formName = "Pride Rookidee V2",
    )
    val prideBoltundV1 = boltund.copy(
      id = 100000065,
      identifier = "pride-boltund-v1",
      name = "Pride Boltund V1",
      isDefault = false,
      formName = "Pride Boltund V1",
    )
    val prideBoltundV2 = boltund.copy(
      id = 100000066,
      identifier = "pride-boltund-v2",
      name = "Pride Boltund V2",
      isDefault = false,
      formName = "Pride Boltund V2",
    )
    val prideRunerigusV1 = runerigus.copy(
      id = 100000067,
      identifier = "pride-runerigus-v1",
      name = "Pride Runerigus V1",
      isDefault = false,
      formName = "Pride Runerigus V1",
    )
    val prideRunerigusV2 = runerigus.copy(
      id = 100000068,
      identifier = "pride-runerigus-v2",
      name = "Pride Runerigus V2",
      isDefault = false,
      formName = "Pride Runerigus V2",
    )
    val prideArctovishV1 = arctovish.copy(
      id = 100000069,
      identifier = "pride-arctovish-v1",
      name = "Pride Arctovish V1",
      isDefault = false,
      formName = "Pride Arctovish V1",
    )
    val prideArctovishV2 = arctovish.copy(
      id = 100000070,
      identifier = "pride-arctovish-v2",
      name = "Pride Arctovish V2",
      isDefault = false,
      formName = "Pride Arctovish V2",
    )
    //Register custom Pokémon (use updated attributes)
    registerCustomPokemon(
      prideCharizardV1,
      prideCharizardV1.identifier,
      prideCharizardV1.name,
      PokemonType(prideCharizardV1.id, charizard.types.map { it.id }),
      PokemonStat.getByPokemonId(charizard.id).map {
        it.copy(id = prideCharizardV1.id)
      }
    )
    registerCustomPokemon(
      prideCharizardV2,
      prideCharizardV2.identifier,
      prideCharizardV2.name,
      PokemonType(prideCharizardV2.id, charizard.types.map { it.id }),
      PokemonStat.getByPokemonId(charizard.id).map {
        it.copy(id = prideCharizardV2.id)
      }
    )
    registerCustomPokemon(
      pridePidgeyV1,
      pridePidgeyV1.identifier,
      pridePidgeyV1.name,
      PokemonType(pridePidgeyV1.id, pidgey.types.map { it.id }),
      PokemonStat.getByPokemonId(pidgey.id).map {
        it.copy(id = pridePidgeyV1.id)
      }
    )
    registerCustomPokemon(
      pridePidgeyV2,
      pridePidgeyV2.identifier,
      pridePidgeyV2.name,
      PokemonType(pridePidgeyV2.id, pidgey.types.map { it.id }),
      PokemonStat.getByPokemonId(pidgey.id).map {
        it.copy(id = pridePidgeyV2.id)
      }
    )
    registerCustomPokemon(
      pridePikachuV1,
      pridePikachuV1.identifier,
      pridePikachuV1.name,
      PokemonType(pridePikachuV1.id, pikachu.types.map { it.id }),
      PokemonStat.getByPokemonId(pikachu.id).map {
        it.copy(id = pridePikachuV1.id)
      }
    )
    registerCustomPokemon(
      pridePikachuV2,
      pridePikachuV2.identifier,
      pridePikachuV2.name,
      PokemonType(pridePikachuV2.id, pikachu.types.map { it.id }),
      PokemonStat.getByPokemonId(pikachu.id).map {
        it.copy(id = pridePikachuV2.id)
      }
    )
    registerCustomPokemon(
      prideNidoranFV1,
      prideNidoranFV1.identifier,
      prideNidoranFV1.name,
      PokemonType(prideNidoranFV1.id, nidoranF.types.map { it.id }),
      PokemonStat.getByPokemonId(nidoranF.id).map {
        it.copy(id = prideNidoranFV1.id)
      }
    )
    registerCustomPokemon(
      prideNidoranFV2,
      prideNidoranFV2.identifier,
      prideNidoranFV2.name,
      PokemonType(prideNidoranFV2.id, nidoranF.types.map { it.id }),
      PokemonStat.getByPokemonId(nidoranF.id).map {
        it.copy(id = prideNidoranFV2.id)
      }
    )
    registerCustomPokemon(
      prideVenomothV1,
      prideVenomothV1.identifier,
      prideVenomothV1.name,
      PokemonType(prideVenomothV1.id, venomoth.types.map { it.id }),
      PokemonStat.getByPokemonId(venomoth.id).map {
        it.copy(id = prideVenomothV1.id)
      }
    )
    registerCustomPokemon(
      prideVenomothV2,
      prideVenomothV2.identifier,
      prideVenomothV2.name,
      PokemonType(prideVenomothV2.id, venomoth.types.map { it.id }),
      PokemonStat.getByPokemonId(venomoth.id).map {
        it.copy(id = prideVenomothV2.id)
      }
    )
    registerCustomPokemon(
      prideMankeyV1,
      prideMankeyV1.identifier,
      prideMankeyV1.name,
      PokemonType(prideMankeyV1.id, mankey.types.map { it.id }),
      PokemonStat.getByPokemonId(mankey.id).map {
        it.copy(id = prideMankeyV1.id)
      }
    )
    registerCustomPokemon(
      prideMankeyV2,
      prideMankeyV2.identifier,
      prideMankeyV2.name,
      PokemonType(prideMankeyV2.id, mankey.types.map { it.id }),
      PokemonStat.getByPokemonId(mankey.id).map {
        it.copy(id = prideMankeyV2.id)
      }
    )
    registerCustomPokemon(
      prideChanseyV1,
      prideChanseyV1.identifier,
      prideChanseyV1.name,
      PokemonType(prideChanseyV1.id, chansey.types.map { it.id }),
      PokemonStat.getByPokemonId(chansey.id).map {
        it.copy(id = prideChanseyV1.id)
      }
    )
    registerCustomPokemon(
      prideChanseyV2,
      prideChanseyV2.identifier,
      prideChanseyV2.name,
      PokemonType(prideChanseyV2.id, chansey.types.map { it.id }),
      PokemonStat.getByPokemonId(chansey.id).map {
        it.copy(id = prideChanseyV2.id)
      }
    )
    registerCustomPokemon(
      prideEeveeV1,
      prideEeveeV1.identifier,
      prideEeveeV1.name,
      PokemonType(prideEeveeV1.id, eevee.types.map { it.id }),
      PokemonStat.getByPokemonId(eevee.id).map {
        it.copy(id = prideEeveeV1.id)
      }
    )
    registerCustomPokemon(
      prideEeveeV2,
      prideEeveeV2.identifier,
      prideEeveeV2.name,
      PokemonType(prideEeveeV2.id, eevee.types.map { it.id }),
      PokemonStat.getByPokemonId(eevee.id).map {
        it.copy(id = prideEeveeV2.id)
      }
    )
    registerCustomPokemon(
      prideMewtwoV1,
      prideMewtwoV1.identifier,
      prideMewtwoV1.name,
      PokemonType(prideMewtwoV1.id, mewtwo.types.map { it.id }),
      PokemonStat.getByPokemonId(mewtwo.id).map {
        it.copy(id = prideMewtwoV1.id)
      }
    )
    registerCustomPokemon(
      prideMewtwoV2,
      prideMewtwoV2.identifier,
      prideMewtwoV2.name,
      PokemonType(prideMewtwoV2.id, mewtwo.types.map { it.id }),
      PokemonStat.getByPokemonId(mewtwo.id).map {
        it.copy(id = prideMewtwoV2.id)
      }
    )
    registerCustomPokemon(
      prideUmbreonV1,
      prideUmbreonV1.identifier,
      prideUmbreonV1.name,
      PokemonType(prideUmbreonV1.id, umbreon.types.map { it.id }),
      PokemonStat.getByPokemonId(umbreon.id).map {
        it.copy(id = prideUmbreonV1.id)
      }
    )
    registerCustomPokemon(
      prideUmbreonV2,
      prideUmbreonV2.identifier,
      prideUmbreonV2.name,
      PokemonType(prideUmbreonV2.id, umbreon.types.map { it.id }),
      PokemonStat.getByPokemonId(umbreon.id).map {
        it.copy(id = prideUmbreonV2.id)
      }
    )
    registerCustomPokemon(
      prideLinooneV1,
      prideLinooneV1.identifier,
      prideLinooneV1.name,
      PokemonType(prideLinooneV1.id, linoone.types.map { it.id }),
      PokemonStat.getByPokemonId(linoone.id).map {
        it.copy(id = prideLinooneV1.id)
      }
    )
    registerCustomPokemon(
      prideLinooneV2,
      prideLinooneV2.identifier,
      prideLinooneV2.name,
      PokemonType(prideLinooneV2.id, linoone.types.map { it.id }),
      PokemonStat.getByPokemonId(linoone.id).map {
        it.copy(id = prideLinooneV2.id)
      }
    )
    registerCustomPokemon(
      prideWingullV1,
      prideWingullV1.identifier,
      prideWingullV1.name,
      PokemonType(prideWingullV1.id, wingull.types.map { it.id }),
      PokemonStat.getByPokemonId(wingull.id).map {
        it.copy(id = prideWingullV1.id)
      }
    )
    registerCustomPokemon(
      prideWingullV2,
      prideWingullV2.identifier,
      prideWingullV2.name,
      PokemonType(prideWingullV2.id, wingull.types.map { it.id }),
      PokemonStat.getByPokemonId(wingull.id).map {
        it.copy(id = prideWingullV2.id)
      }
    )
    registerCustomPokemon(
      prideMinunV1,
      prideMinunV1.identifier,
      prideMinunV1.name,
      PokemonType(prideMinunV1.id, minun.types.map { it.id }),
      PokemonStat.getByPokemonId(minun.id).map {
        it.copy(id = prideMinunV1.id)
      }
    )
    registerCustomPokemon(
      prideMinunV2,
      prideMinunV2.identifier,
      prideMinunV2.name,
      PokemonType(prideMinunV2.id, minun.types.map { it.id }),
      PokemonStat.getByPokemonId(minun.id).map {
        it.copy(id = prideMinunV2.id)
      }
    )
    registerCustomPokemon(
      prideWailordV1,
      prideWailordV1.identifier,
      prideWailordV1.name,
      PokemonType(prideWailordV1.id, wailord.types.map { it.id }),
      PokemonStat.getByPokemonId(wailord.id).map {
        it.copy(id = prideWailordV1.id)
      }
    )
    registerCustomPokemon(
      prideWailordV2,
      prideWailordV2.identifier,
      prideWailordV2.name,
      PokemonType(prideWailordV2.id, wailord.types.map { it.id }),
      PokemonStat.getByPokemonId(wailord.id).map {
        it.copy(id = prideWailordV2.id)
      }
    )
    registerCustomPokemon(
      prideLunatoneV1,
      prideLunatoneV1.identifier,
      prideLunatoneV1.name,
      PokemonType(prideLunatoneV1.id, lunatone.types.map { it.id }),
      PokemonStat.getByPokemonId(lunatone.id).map {
        it.copy(id = prideLunatoneV1.id)
      }
    )
    registerCustomPokemon(
      prideLunatoneV2,
      prideLunatoneV2.identifier,
      prideLunatoneV2.name,
      PokemonType(prideLunatoneV2.id, lunatone.types.map { it.id }),
      PokemonStat.getByPokemonId(lunatone.id).map {
        it.copy(id = prideLunatoneV2.id)
      }
    )
    registerCustomPokemon(
      prideSolrockV1,
      prideSolrockV1.identifier,
      prideSolrockV1.name,
      PokemonType(prideSolrockV1.id, solrock.types.map { it.id }),
      PokemonStat.getByPokemonId(solrock.id).map {
        it.copy(id = prideSolrockV1.id)
      }
    )
    registerCustomPokemon(
      prideSolrockV2,
      prideSolrockV2.identifier,
      prideSolrockV2.name,
      PokemonType(prideSolrockV2.id, solrock.types.map { it.id }),
      PokemonStat.getByPokemonId(solrock.id).map {
        it.copy(id = prideSolrockV2.id)
      }
    )
    registerCustomPokemon(
      prideMiloticV1,
      prideMiloticV1.identifier,
      prideMiloticV1.name,
      PokemonType(prideMiloticV1.id, milotic.types.map { it.id }),
      PokemonStat.getByPokemonId(milotic.id).map {
        it.copy(id = prideMiloticV1.id)
      }
    )
    registerCustomPokemon(
      prideMiloticV2,
      prideMiloticV2.identifier,
      prideMiloticV2.name,
      PokemonType(prideMiloticV2.id, milotic.types.map { it.id }),
      PokemonStat.getByPokemonId(milotic.id).map {
        it.copy(id = prideMiloticV2.id)
      }
    )
    registerCustomPokemon(
      prideRayquazaV1,
      prideRayquazaV1.identifier,
      prideRayquazaV1.name,
      PokemonType(prideRayquazaV1.id, rayquaza.types.map { it.id }),
      PokemonStat.getByPokemonId(rayquaza.id).map {
        it.copy(id = prideRayquazaV1.id)
      }
    )
    registerCustomPokemon(
      prideRayquazaV2,
      prideRayquazaV2.identifier,
      prideRayquazaV2.name,
      PokemonType(prideRayquazaV2.id, rayquaza.types.map { it.id }),
      PokemonStat.getByPokemonId(rayquaza.id).map {
        it.copy(id = prideRayquazaV2.id)
      }
    )
    registerCustomPokemon(
      pridePrinplupV1,
      pridePrinplupV1.identifier,
      pridePrinplupV1.name,
      PokemonType(pridePrinplupV1.id, prinplup.types.map { it.id }),
      PokemonStat.getByPokemonId(prinplup.id).map {
        it.copy(id = pridePrinplupV1.id)
      }
    )
    registerCustomPokemon(
      pridePrinplupV2,
      pridePrinplupV2.identifier,
      pridePrinplupV2.name,
      PokemonType(pridePrinplupV2.id, prinplup.types.map { it.id }),
      PokemonStat.getByPokemonId(prinplup.id).map {
        it.copy(id = pridePrinplupV2.id)
      }
    )
    registerCustomPokemon(
      prideWormadamV1,
      prideWormadamV1.identifier,
      prideWormadamV1.name,
      PokemonType(prideWormadamV1.id, wormadam.types.map { it.id }),
      PokemonStat.getByPokemonId(wormadam.id).map {
        it.copy(id = prideWormadamV1.id)
      }
    )
    registerCustomPokemon(
      prideWormadamV2,
      prideWormadamV2.identifier,
      prideWormadamV2.name,
      PokemonType(prideWormadamV2.id, wormadam.types.map { it.id }),
      PokemonStat.getByPokemonId(wormadam.id).map {
        it.copy(id = prideWormadamV2.id)
      }
    )
    registerCustomPokemon(
      prideGarchompV1,
      prideGarchompV1.identifier,
      prideGarchompV1.name,
      PokemonType(prideGarchompV1.id, garchomp.types.map { it.id }),
      PokemonStat.getByPokemonId(garchomp.id).map {
        it.copy(id = prideGarchompV1.id)
      }
    )
    registerCustomPokemon(
      prideGarchompV2,
      prideGarchompV2.identifier,
      prideGarchompV2.name,
      PokemonType(prideGarchompV2.id, garchomp.types.map { it.id }),
      PokemonStat.getByPokemonId(garchomp.id).map {
        it.copy(id = prideGarchompV2.id)
      }
    )
    registerCustomPokemon(
      prideLucarioV1,
      prideLucarioV1.identifier,
      prideLucarioV1.name,
      PokemonType(prideLucarioV1.id, lucario.types.map { it.id }),
      PokemonStat.getByPokemonId(lucario.id).map {
        it.copy(id = prideLucarioV1.id)
      }
    )
    registerCustomPokemon(
      prideLucarioV2,
      prideLucarioV2.identifier,
      prideLucarioV2.name,
      PokemonType(prideLucarioV2.id, lucario.types.map { it.id }),
      PokemonStat.getByPokemonId(lucario.id).map {
        it.copy(id = prideLucarioV2.id)
      }
    )
    registerCustomPokemon(
      prideDarkraiV1,
      prideDarkraiV1.identifier,
      prideDarkraiV1.name,
      PokemonType(prideDarkraiV1.id, darkrai.types.map { it.id }),
      PokemonStat.getByPokemonId(darkrai.id).map {
        it.copy(id = prideDarkraiV1.id)
      }
    )
    registerCustomPokemon(
      prideDarkraiV2,
      prideDarkraiV2.identifier,
      prideDarkraiV2.name,
      PokemonType(prideDarkraiV2.id, darkrai.types.map { it.id }),
      PokemonStat.getByPokemonId(darkrai.id).map {
        it.copy(id = prideDarkraiV2.id)
      }
    )
    registerCustomPokemon(
      prideArceusV1,
      prideArceusV1.identifier,
      prideArceusV1.name,
      PokemonType(prideArceusV1.id, arceus.types.map { it.id }),
      PokemonStat.getByPokemonId(arceus.id).map {
        it.copy(id = prideArceusV1.id)
      }
    )
    registerCustomPokemon(
      prideArceusV2,
      prideArceusV2.identifier,
      prideArceusV2.name,
      PokemonType(prideArceusV2.id, arceus.types.map { it.id }),
      PokemonStat.getByPokemonId(arceus.id).map {
        it.copy(id = prideArceusV2.id)
      }
    )
    registerCustomPokemon(
      prideSerperiorV1,
      prideSerperiorV1.identifier,
      prideSerperiorV1.name,
      PokemonType(prideSerperiorV1.id, serperior.types.map { it.id }),
      PokemonStat.getByPokemonId(serperior.id).map {
        it.copy(id = prideSerperiorV1.id)
      }
    )
    registerCustomPokemon(
      prideSerperiorV2,
      prideSerperiorV2.identifier,
      prideSerperiorV2.name,
      PokemonType(prideSerperiorV2.id, serperior.types.map { it.id }),
      PokemonStat.getByPokemonId(serperior.id).map {
        it.copy(id = prideSerperiorV2.id)
      }
    )
    registerCustomPokemon(
      prideDewottV1,
      prideDewottV1.identifier,
      prideDewottV1.name,
      PokemonType(prideDewottV1.id, dewott.types.map { it.id }),
      PokemonStat.getByPokemonId(dewott.id).map {
        it.copy(id = prideDewottV1.id)
      }
    )
    registerCustomPokemon(
      prideDewottV2,
      prideDewottV2.identifier,
      prideDewottV2.name,
      PokemonType(prideDewottV2.id, dewott.types.map { it.id }),
      PokemonStat.getByPokemonId(dewott.id).map {
        it.copy(id = prideDewottV2.id)
      }
    )
    registerCustomPokemon(
      prideGreninjaV1,
      prideGreninjaV1.identifier,
      prideGreninjaV1.name,
      PokemonType(prideGreninjaV1.id, greninja.types.map { it.id }),
      PokemonStat.getByPokemonId(greninja.id).map {
        it.copy(id = prideGreninjaV1.id)
      }
    )
    registerCustomPokemon(
      prideGreninjaV2,
      prideGreninjaV2.identifier,
      prideGreninjaV2.name,
      PokemonType(prideGreninjaV2.id, greninja.types.map { it.id }),
      PokemonStat.getByPokemonId(greninja.id).map {
        it.copy(id = prideGreninjaV2.id)
      }
    )
    registerCustomPokemon(
      prideTrevenantV1,
      prideTrevenantV1.identifier,
      prideTrevenantV1.name,
      PokemonType(prideTrevenantV1.id, trevenant.types.map { it.id }),
      PokemonStat.getByPokemonId(trevenant.id).map {
        it.copy(id = prideTrevenantV1.id)
      }
    )
    registerCustomPokemon(
      prideTrevenantV2,
      prideTrevenantV2.identifier,
      prideTrevenantV2.name,
      PokemonType(prideTrevenantV2.id, trevenant.types.map { it.id }),
      PokemonStat.getByPokemonId(trevenant.id).map {
        it.copy(id = prideTrevenantV2.id)
      }
    )
    registerCustomPokemon(
      prideBergmiteV1,
      prideBergmiteV1.identifier,
      prideBergmiteV1.name,
      PokemonType(prideBergmiteV1.id, bergmite.types.map { it.id }),
      PokemonStat.getByPokemonId(bergmite.id).map {
        it.copy(id = prideBergmiteV1.id)
      }
    )
    registerCustomPokemon(
      prideBergmiteV2,
      prideBergmiteV2.identifier,
      prideBergmiteV2.name,
      PokemonType(prideBergmiteV2.id, bergmite.types.map { it.id }),
      PokemonStat.getByPokemonId(bergmite.id).map {
        it.copy(id = prideBergmiteV2.id)
      }
    )
    registerCustomPokemon(
      prideYveltalV1,
      prideYveltalV1.identifier,
      prideYveltalV1.name,
      PokemonType(prideYveltalV1.id, yveltal.types.map { it.id }),
      PokemonStat.getByPokemonId(yveltal.id).map {
        it.copy(id = prideYveltalV1.id)
      }
    )
    registerCustomPokemon(
      prideYveltalV2,
      prideYveltalV2.identifier,
      prideYveltalV2.name,
      PokemonType(prideYveltalV2.id, yveltal.types.map { it.id }),
      PokemonStat.getByPokemonId(yveltal.id).map {
        it.copy(id = prideYveltalV2.id)
      }
    )
    registerCustomPokemon(
      prideTogedemaruV1,
      prideTogedemaruV1.identifier,
      prideTogedemaruV1.name,
      PokemonType(prideTogedemaruV1.id, togedemaru.types.map { it.id }),
      PokemonStat.getByPokemonId(togedemaru.id).map {
        it.copy(id = prideTogedemaruV1.id)
      }
    )
    registerCustomPokemon(
      prideTogedemaruV2,
      prideTogedemaruV2.identifier,
      prideTogedemaruV2.name,
      PokemonType(prideTogedemaruV2.id, togedemaru.types.map { it.id }),
      PokemonStat.getByPokemonId(togedemaru.id).map {
        it.copy(id = prideTogedemaruV2.id)
      }
    )
    registerCustomPokemon(
      prideRookideeV1,
      prideRookideeV1.identifier,
      prideRookideeV1.name,
      PokemonType(prideRookideeV1.id, rookidee.types.map { it.id }),
      PokemonStat.getByPokemonId(rookidee.id).map {
        it.copy(id = prideRookideeV1.id)
      }
    )
    registerCustomPokemon(
      prideRookideeV2,
      prideRookideeV2.identifier,
      prideRookideeV2.name,
      PokemonType(prideRookideeV2.id, rookidee.types.map { it.id }),
      PokemonStat.getByPokemonId(rookidee.id).map {
        it.copy(id = prideRookideeV2.id)
      }
    )
    registerCustomPokemon(
      prideBoltundV1,
      prideBoltundV1.identifier,
      prideBoltundV1.name,
      PokemonType(prideBoltundV1.id, boltund.types.map { it.id }),
      PokemonStat.getByPokemonId(boltund.id).map {
        it.copy(id = prideBoltundV1.id)
      }
    )
    registerCustomPokemon(
      prideBoltundV2,
      prideBoltundV2.identifier,
      prideBoltundV2.name,
      PokemonType(prideBoltundV2.id, boltund.types.map { it.id }),
      PokemonStat.getByPokemonId(boltund.id).map {
        it.copy(id = prideBoltundV2.id)
      }
    )
    registerCustomPokemon(
      prideRunerigusV1,
      prideRunerigusV1.identifier,
      prideRunerigusV1.name,
      PokemonType(prideRunerigusV1.id, runerigus.types.map { it.id }),
      PokemonStat.getByPokemonId(runerigus.id).map {
        it.copy(id = prideRunerigusV1.id)
      }
    )
    registerCustomPokemon(
      prideRunerigusV2,
      prideRunerigusV2.identifier,
      prideRunerigusV2.name,
      PokemonType(prideRunerigusV2.id, runerigus.types.map { it.id }),
      PokemonStat.getByPokemonId(runerigus.id).map {
        it.copy(id = prideRunerigusV2.id)
      }
    )
    registerCustomPokemon(
      prideArctovishV1,
      prideArctovishV1.identifier,
      prideArctovishV1.name,
      PokemonType(prideArctovishV1.id, arctovish.types.map { it.id }),
      PokemonStat.getByPokemonId(arctovish.id).map {
        it.copy(id = prideArctovishV1.id)
      }
    )
    registerCustomPokemon(
      prideArctovishV2,
      prideArctovishV2.identifier,
      prideArctovishV2.name,
      PokemonType(prideArctovishV2.id, arctovish.types.map { it.id }),
      PokemonStat.getByPokemonId(arctovish.id).map {
        it.copy(id = prideArctovishV2.id)
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