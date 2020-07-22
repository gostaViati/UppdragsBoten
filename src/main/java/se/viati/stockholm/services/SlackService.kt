package se.viati.stockholm.services

import net.gpedro.integrations.slack.SlackApi
import net.gpedro.integrations.slack.SlackMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import se.viati.stockholm.services.domain.Assignment

import javax.annotation.PostConstruct

@Service
class SlackService {

  @Value("\${slack.enabled}")
  private val slackEnabled: Boolean = false
  @Value("\${slack.webhook}")
  private val slackWebHook: String? = null
  @Value("\${slack.channel.full}")
  private val channelFull: String? = null
  @Value("\${slack.channel.titles}")
  private val channelTitles: String? = null

  private lateinit var slackApi: SlackApi

  private val logger = LoggerFactory.getLogger(this::class.java)

  @PostConstruct
  fun init() {
    slackApi = SlackApi(slackWebHook!!)
  }

  fun postToSlack(assignment: Assignment) {
    val assignmentTitle = "*${assignment.title}* ${assignment.source?.let { "[$it]" } ?: ""}"

    logger.info("${if (slackEnabled) "Posting" else "Not posting (disabled)"} to Slack: $assignmentTitle, mails: ${assignment.originatingMailIds.size}")

    if (slackEnabled) {
      val messageWithBody = SlackMessage(channelFull,
          null, "$assignmentTitle\n${assignment.description}")
      slackApi.call(messageWithBody)

      val messageWithOnlyTitle = SlackMessage(channelTitles,
          null, assignmentTitle)
      slackApi.call(messageWithOnlyTitle)
    }
  }
}
