package se.viati.stockholm.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.viati.stockholm.services.domain.Mail;

import javax.annotation.PostConstruct;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IMAPMailClient implements MailClient {

  @Value("${email.username}")
  private String userName;
  @Value("${email.password}")
  private String password;
  @Value("${email.imap.server}")
  private String imapServer;
  @Value("${email.imap.folder}")
  private String imapFolder;

  private static final Logger logger = LoggerFactory.getLogger(IMAPMailClient.class);
  private Session session;

  @PostConstruct
  public void init() {
    final Properties props = System.getProperties();
    props.put("mail.store.protocol", "imaps");
    props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.put("mail.imap.socketFactory.fallback", "false");
    //props.put("mail.debug", "true");
    //props.put("mail.debug.auth", "true");
    session = Session.getInstance(props);
  }

  public List<Mail> getLatestMails(int mailsToGet) {
    Store store = null;
    Folder inbox = null;
    try {
      //session.setDebug(true);
      store = session.getStore("imaps");

      logger.info("Connecting to IMAP " + userName + " @ " + imapServer + "with password length " + password.length());
      store.connect(imapServer, userName, password);
      logger.info("Connected to IMAP");
      inbox = store.getFolder(imapFolder);
      inbox.open(Folder.READ_ONLY);
      logger.info("Getting emails from folder " + imapFolder);

      final Message[] messages = inbox.getMessages();
      logger.info("Total messages in folder: " + messages.length);
      int firstEmail = Math.max(0, messages.length-mailsToGet);
      logger.info("Getting messages with index " + firstEmail + " (incl) to " + messages.length + " (excl)");

      return Arrays.stream(messages, firstEmail, messages.length)
          .map(message -> {
            try {
              final String id = Arrays.toString(message.getHeader("Message-ID"));
              final String subject = message.getSubject();
              final String body = getTextFromMessage(message);
              return new Mail(id, subject, body);
            } catch (MessagingException | IOException e) {
              logger.error("Error when mapping message to Mail", e);
            }
            return null;
          }).filter(Objects::nonNull).collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error", e);
    } finally {
      if (inbox != null) {
        try {
          inbox.close(false);
        } catch (MessagingException ex) {
          // Do nothing but caught to close the store below.
        }
      }

      if (store != null) {
        try {
          store.close();
        } catch (MessagingException ex) {
          // Do nothing
        }
      }
    }
    return Collections.emptyList();
  }

  private String getTextFromMessage(Message message) throws MessagingException, IOException {
    if (message.isMimeType("text/plain")) {
      return message.getContent().toString();
    }
    if (message.isMimeType("multipart/*")) {
      MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
      return getTextFromMimeMultipart(mimeMultipart);
    }
    return "";
  }

  private String getTextFromMimeMultipart(
      MimeMultipart mimeMultipart)  throws MessagingException, IOException{
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < mimeMultipart.getCount(); i++) {
      BodyPart bodyPart = mimeMultipart.getBodyPart(i);
      if (bodyPart.isMimeType("text/plain")) {
        return result + "\n" + bodyPart.getContent(); // without return, same text appears twice in my tests
      }
      result.append(this.parseBodyPart(bodyPart));
    }
    return result.toString();
  }

  private String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException {
    if (bodyPart.isMimeType("text/html")) {
      return "\n" + org.jsoup.Jsoup
          .parse(bodyPart.getContent().toString())
          .text();
    }
    if (bodyPart.getContent() instanceof MimeMultipart){
      return getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
    }

    return "";
  }
}
