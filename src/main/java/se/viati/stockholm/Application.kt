package se.viati.stockholm

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.scheduling.annotation.EnableScheduling
import se.viati.stockholm.services.PostMailsToSlackService

@SpringBootApplication
@EnableScheduling
class Application

fun main(args: Array<String>) {
  val runApplication = runApplication<Application>(*args)

  logBuildInfo(runApplication)

  // At startup, call the same function as the scheduled job
  runApplication.getBean(PostMailsToSlackService::class.java).getMailsAndPostToSlack()
}

fun logBuildInfo(runApplication: ConfigurableApplicationContext) {
  val logger = LoggerFactory.getLogger(PostMailsToSlackService::class.java)
  val buildProperties = runApplication.getBean(BuildProperties::class.java)
  logger.info("Starting ${buildProperties.name} v${buildProperties.version} built ${buildProperties.time}")
}

