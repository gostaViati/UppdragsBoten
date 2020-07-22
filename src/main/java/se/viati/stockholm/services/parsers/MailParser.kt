package se.viati.stockholm.services.parsers

import se.viati.stockholm.services.domain.Assignment
import se.viati.stockholm.services.domain.Mail

/**
 * https://jsoup.org/cookbook/extracting-data/selector-syntax
 */
interface MailParser {
  fun isMailParseble(mail: Mail): Boolean
  fun parserName(): String
  fun parseMail(mail: Mail): List<Assignment>
}