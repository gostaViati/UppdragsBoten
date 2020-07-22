package se.viati.stockholm.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.ResponseEntity.status
import org.springframework.web.bind.annotation.*
import se.viati.stockholm.services.DatabaseService
import se.viati.stockholm.services.PostMailsToSlackService

@RestController
class MailController(
    val databaseService: DatabaseService,
    val postMailsToSlackService: PostMailsToSlackService
) {

  @PutMapping("/run")
  fun run() = postMailsToSlackService.getMailsAndPostToSlack()

  @GetMapping("/mails/count")
  fun count() = databaseService.count()

  @PutMapping("/mails/init/{initialId}")
  fun init(@PathVariable initialId: String): ResponseEntity<Void> =
      if (databaseService.createDatabaseIfNotExists(initialId)) {
        ok().build()
      } else {
        status(HttpStatus.INTERNAL_SERVER_ERROR).build()
      }
}