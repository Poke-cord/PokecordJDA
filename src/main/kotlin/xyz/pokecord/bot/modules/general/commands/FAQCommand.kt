package xyz.pokecord.bot.modules.general.commands

import xyz.pokecord.bot.api.ICommandContext
import xyz.pokecord.bot.core.managers.I18n
import xyz.pokecord.bot.core.structures.discord.base.Command
import xyz.pokecord.bot.utils.EmbedPaginator
import kotlin.math.ceil

class FAQCommand : Command() {
  override val name = "FAQ"

  @Executor
  suspend fun execute(
    context: ICommandContext,
    @Argument(optional = true, consumeRest = true) keyword: String?
  ) {
    val faqCount = module.bot.database.faqRepository.getFaqCount(keyword).toInt()
    val pageCount = if (keyword == null) ceil(faqCount / 10.0).toInt() else faqCount
    val language = context.getLanguage()
    val embedPaginator =
      EmbedPaginator(context, pageCount, {
        val faqs = module.bot.database.faqRepository.getFaqs(
          keyword,
          skip = if (keyword == null) it * 10 else it,
          limit = if (keyword == null) 10 else null
        )
        if (keyword == null) context.embedTemplates.normal(
          faqs.joinToString("\n") { faq ->
            "${faq.id} - ${faq.keywords.joinToString(", ")}"
          },
          context.translate("modules.general.commands.faq.list.embed.title")
        ) else {
          val faq = faqs.firstOrNull()
          if (faq === null) {
            context.embedTemplates.error(context.translate("modules.general.commands.faq.errors.noFaqFound"))
          } else {
            val translation = faq.translations.find { translation -> translation.language == language }
              ?: faq.translations.find { translation -> translation.language == I18n.Language.EN_US }!!
            context.embedTemplates.normal(
              "```\n${translation.answer}\n```\nKeywords:\n```\n${
                listOf(
                  faq.id,
                  *faq.keywords.toTypedArray()
                ).joinToString(", ")
              }\n```", translation.question
            )
          }
        }
      })
    embedPaginator.start()
  }
}
