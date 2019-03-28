package se.viati.stockholm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import se.viati.stockholm.services.domain.Mail;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * https://github.com/OfficeDev/ews-java-api/wiki/Getting-Started-Guide#using-the-library
 *
 */
@Service
public class PostMailsToSlackService {

    private static final String[] NOT_NEEDED_TEXT = {
            "meOneförfrågan för ", "ITC Network söker till ", "ITC Network söker för ", "ITC söker en ", "Nytt konsultbehov -",
            "Konsultbehov - ", "OBS!!! Ny förfrågan ", "OBS!!! Ny internförfrågan ",
            "Nytt konsultbehov: ", "ITC Network söker en ", "ITC Netwok söker en ", "Antigo - ",
            "ITC Network söker för kunds räkning en ", "ITC söker ", "ITC Network ", "meOneförfrågan - ",
            "meOneförfrågan, ", "meOne request for ", "meOnerequest - ", "meOne förfrågan - ", "söker ", "*", "VB: ", "SV: ",
            "meOneförfrågning för "};
    private static final int MAILS_TO_GET = 151;

    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private ExchangeMailService exchangeMailService;
    @Autowired
    private SlackService slackService;

    private static final Logger logger = LoggerFactory.getLogger(PostMailsToSlackService.class);

    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void getMailsAndPostToSlack() throws Exception {
        logger.info("Get latest emails and post the new ones to Slack");
        if (databaseService.isDatabaseInitialized()) {
            final Stream<Mail> mailItemStream = getLatestMails(MAILS_TO_GET);
            postMailItemsToSlack(mailItemStream);
            logger.info("Done!");
        } else {
            logger.info("Database is not initialized, will not fetch any mails.");
        }
    }

    public Stream<Mail> getLatestMails(int mailsToGet) throws Exception {
        return exchangeMailService.getLatestMails(mailsToGet);
    }

    private void postMailItemsToSlack(Stream<Mail> mailItemsStream) {
        mailItemsStream
                .filter(mail -> !databaseService.existsId(mail.id))
                .map(this::removeNotNeededTextFromSubject)
                .forEach(mail -> {
                    slackService.postToSlack(mail);
                    databaseService.insertId(mail.id);
                });
    }

    private Mail removeNotNeededTextFromSubject(Mail mail) {
        String newSubject = Arrays.stream(NOT_NEEDED_TEXT)
                .reduce(mail.subject, (subject, notNeededText) ->
                        subject.replace(notNeededText, ""))
                .trim();
        return new Mail(mail.id, newSubject, mail.body);
    }

}
