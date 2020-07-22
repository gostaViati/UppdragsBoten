package se.viati.stockholm.services.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Whitelist
import org.jsoup.select.Elements
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import se.viati.stockholm.services.domain.Assignment
import se.viati.stockholm.services.domain.Mail

@Component
class ForfragningarMailParser : MailParser {

  private val logger = LoggerFactory.getLogger(this::class.java)

  override fun isMailParseble(mail: Mail) = mail.subject.equals("Förfrågningar")

  override fun parserName() = "Viati-förfrågningar"

  override fun parseMail(mail: Mail): List<Assignment> =
      try {
        Jsoup.parse(mail.body)
            .outputSettings(Document.OutputSettings().prettyPrint(false))//makes html() preserve linebreaks and spacing
            .select("li.MsoListParagraph")
            .map {
              Assignment(
                  originatingMailId = mail.id,
                  title = cleanHtml(it),
                  description = "",
                  source = parserName()
              )
            }.filterNot {
              it.title.equals(other = "Inga nya", ignoreCase = true)
            }.toList()
      } catch (e: Throwable) {
        logger.error("Error when parsing mail: ${e.message}", e)
        emptyList()
      }

  private fun cleanHtml(it: Element) =
      Jsoup.clean(it.html(), "", Whitelist.none(), Document.OutputSettings().prettyPrint(true))
          .replace("&nbsp;", " ")

}