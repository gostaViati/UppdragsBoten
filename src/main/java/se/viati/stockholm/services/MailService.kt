package se.viati.stockholm.services

import org.springframework.stereotype.Service
import se.viati.stockholm.services.domain.Mail

@Service
class MailService(
        val mailClient: MailClient
) {
    fun getLatestMails(mailsToGet: Int): List<Mail> =
            mailClient.getLatestMails(mailsToGet)
}
