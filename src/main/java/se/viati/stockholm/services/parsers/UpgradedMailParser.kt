package se.viati.stockholm.services.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import se.viati.stockholm.services.domain.Assignment
import se.viati.stockholm.services.domain.Mail

@Component
class UpgradedMailParser : MailParser {

  private val logger = LoggerFactory.getLogger(this::class.java)

  override fun isMailParseble(mail: Mail) = mail.subject.contains("Upgraded")

  override fun parserName() = "Upgraded"

  override fun parseMail(mail: Mail): List<Assignment> =
      try {
        Jsoup.parse(mail.body)
            .outputSettings(Document.OutputSettings().prettyPrint(false))//makes html() preserve linebreaks and spacing
            .select("td.x_mcnTextContent")[4]
            .select("a")
            .eachText()
            .map {
              Assignment(
                  originatingMailId = mail.id,
                  title = it,
                  source = parserName()
              )
            }.toList()
      } catch (e: Throwable) {
        logger.error("Error when parsing mail: ${e.message}", e)
        emptyList()
      }
}