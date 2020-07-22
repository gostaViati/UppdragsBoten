package se.viati.stockholm.services

import se.viati.stockholm.services.domain.Mail

import java.util.stream.Stream

interface MailClient {
    fun getLatestMails(mailsToGet: Int): Stream<Mail>
}
