package se.viati.stockholm.services

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import se.viati.stockholm.services.domain.Assignment
import se.viati.stockholm.services.domain.Mail
import se.viati.stockholm.services.parsers.MailParser
import java.util.*

@Service
class PostMailsToSlackService {

  @Autowired
  private lateinit var databaseService: DatabaseService
  @Autowired
  private lateinit var mailService: MailService
  @Autowired
  private lateinit var slackService: SlackService
  @Autowired
  private lateinit var mailParsers: List<MailParser>

  @Scheduled(zone = "Europe/Stockholm", cron = "0 0 18 * * MON-FRI")
  @Throws(Exception::class)
  fun getMailsAndPostToSlack() {
    logger.info("Get latest emails and post the new ones to Slack")
    if (databaseService.isDatabaseInitialized()) {
      val mailItemStream = getLatestMails(MAILS_TO_GET)
      postMailItemsToSlack(mailItemStream)
      logger.info("Done!")
    } else {
      logger.info("Database is not initialized, will not fetch any mails.")
    }
  }

  fun getLatestMails(mailsToGet: Int) =
      mailService.getLatestMails(mailsToGet).toList()


  private fun postMailItemsToSlack(mails: List<Mail>) =
      mails
          .filter { (id) -> !databaseService.existsId(id) }
          .flatMap { this.getAssignmentsFromMail(it) }
          // To remove duplicates, group by title
          .groupingBy { it.title }
          // Accumulate assignments with same title to one assignment holding all originating mail ids
          .reduce { _, accumulator, element ->
            accumulator.addOriginatingIds(element.originatingMailIds)
          }
          .forEach { (_, assignment) ->
            slackService.postToSlack(assignment)
            assignment.originatingMailIds.forEach { id ->
              databaseService.insertIdIfNotExists(id)
            }
          }

  private fun getAssignmentsFromMail(mail: Mail): List<Assignment> =
      mailParsers.firstOrNull { mailParser ->
        mailParser.isMailParseble(mail)
      }?.parseMail(mail)
          ?: listOf(defaultMailParsing(mail))

  private fun defaultMailParsing(mail: Mail): Assignment {
    return Assignment(
        mail.id,
        removeNotNeededTextFromSubject(mail.subject),
        convertHtmlToStringWithLineBreaks(mail.body)
    )
  }

  private fun removeNotNeededTextFromSubject(subject: String) =
      Arrays.stream(NOT_NEEDED_TEXT)
          .reduce(subject) { s, notNeededText -> s.replace(notNeededText, "") }
          .trim { it <= ' ' }


  private fun convertHtmlToStringWithLineBreaks(html: String): String {
    val bodyDocument = Jsoup.parse(html)
    bodyDocument.outputSettings(Document.OutputSettings().prettyPrint(false))//makes html() preserve linebreaks and spacing
    bodyDocument.select("br").append("\\n")
    bodyDocument.select("p").prepend("\\n\\n")
    val s = bodyDocument.html()
        .replace("\\\\n".toRegex(), "\n")
    val htmlAsText = Jsoup.clean(s, "", Whitelist.none(), Document.OutputSettings().prettyPrint(false))
    return htmlAsText
        .replace("&nbsp;", "")
        .replace("&amp;", "&")
        .replace("\n\n\n\n", "\n")
        .replace("\n\n\n", "\n")
        .replace("\n\n", "\n")
  }

  companion object {

    val NOT_NEEDED_TEXT = arrayOf(
        "meOneförfrågan för ",
        "ITC Network söker till ",
        "ITC Network söker för ",
        "ITC söker en ",
        "Nytt konsultbehov -",
        "Konsultbehov - ",
        "OBS!!! Ny förfrågan ",
        "OBS!!! Ny internförfrågan ",
        "Nytt konsultbehov: ",
        "ITC Network söker en ",
        "ITC Netwok söker en ",
        "Antigo - ",
        "ITC Network söker för kunds räkning en ",
        "ITC söker ",
        "ITC Network ",
        "meOneförfrågan - ",
        "meOneförfrågan, ",
        "meOne request for ",
        "meOnerequest - ",
        "meOne förfrågan - ",
        "söker ",
        "*",
        "VB: ",
        "SV: ",
        "Konsultförfrågan från meOne för ",
        "RFI avseende ",
        "Förfrågan ",
        "Förfrågan avseende ",
        "Förfrågan  avseende ",
        "Söker ",
        "1 st ",
        "ITC is looking for a ",
        "ITC is looking for an ",
        "ITC is looking for ",
        "New request – ",
        "Workforce Logiq har behov av ",
        "New Request: ",
        "Ny Förfrågan: ",
        "Ny förfrågan- ",
        "RFI - ",
        "Fwd: ",
        "Requisition: ",
        "Resursbrist is looking for a ",
        "Resursbrist is looking for ",
        "Resursbrist ",
        "Avrop av "
    ).also { it.sortByDescending(String::length) }

    private const val MAILS_TO_GET = 151

    private val logger = LoggerFactory.getLogger(PostMailsToSlackService::class.java)
  }
}
