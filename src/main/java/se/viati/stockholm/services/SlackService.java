package se.viati.stockholm.services;

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.viati.stockholm.services.domain.Mail;

import javax.annotation.PostConstruct;

@Service
public class SlackService {

    @Value("${slack.webhook}")
    private String slackWebHook;
    @Value("${slack.channel.full}")
    private String channelFull;
    @Value("${slack.channel.titles}")
    private String channelTitles;

    private SlackApi slackApi;

    private static final Logger logger = LoggerFactory.getLogger(SlackService.class);

    @PostConstruct
    public void init() {
        slackApi = new SlackApi(slackWebHook);
    }

    public void postToSlack(Mail mail) {
        // Send simple message with custom name
        logger.info("Posting to Slack: " + mail.subject);
        final SlackMessage messageWithBody = new SlackMessage(channelFull,
                null, "*" + mail.subject + "*" +
                "\n" + getHtmlAsStringWithLineBreaks(mail.body));
        slackApi.call(messageWithBody);

        final SlackMessage messageWithOnlySubject = new SlackMessage(channelTitles,
                null, "*" + mail.subject + "*");
        slackApi.call(messageWithOnlySubject);
    }

    private String getHtmlAsStringWithLineBreaks(String html) {
        final Document bodyDocument = Jsoup.parse(html);
        bodyDocument.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        bodyDocument.select("br").append("\\n");
        bodyDocument.select("p").prepend("\\n\\n");
        String s = bodyDocument.html()
                .replaceAll("\\\\n", "\n");
        final String htmlAsText = Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
        return htmlAsText
                .replace("&nbsp;", "")
                .replace("&amp;", "&")
                .replace("\n\n\n\n", "\n")
                .replace("\n\n\n", "\n")
                .replace("\n\n", "\n");
    }

}
