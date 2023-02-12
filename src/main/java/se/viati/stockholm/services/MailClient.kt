package se.viati.stockholm.services

import se.viati.stockholm.services.domain.Mail

interface MailClient {
    fun getLatestMails(mailsToGet: Int): List<Mail>
}
