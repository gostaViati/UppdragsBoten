package se.viati.stockholm.services

import org.springframework.stereotype.Service
import se.viati.stockholm.services.domain.Mail
import java.util.stream.Stream

@Service
class MailService(
        val mailClient: MailClient
) {
    fun getLatestMails(mailsToGet: Int): Stream<Mail> =
            mailClient.getLatestMails(mailsToGet)
}
