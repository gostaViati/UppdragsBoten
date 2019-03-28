package se.viati.stockholm.services;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.viati.stockholm.services.domain.Mail;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

@Service
public class ExchangeMailService {

    @Value("${email.username}")
    private String userName;
    @Value("${email.password}")
    private String password;
    @Value("${email.outlook.url}")
    private String outloookUrl;

    private ExchangeService exchangeService;

    private static final Logger logger = LoggerFactory.getLogger(ExchangeMailService.class);

    @PostConstruct
    public void init() {
        exchangeService = createExchangeService();
        testGettingAnEmail();
    }

    private void testGettingAnEmail() {
        try {
            getLatestMails(1)
                    .findAny()
                    .ifPresent(mail -> {
                        logger.info("Succeeded in getting an email (when testing) (" + mail.subject + ")");
                    });
        } catch (Exception e) {
            logger.error("Failed in getting an email while testing: ", e);
        }
    }

    private ExchangeService createExchangeService() {
        logger.info("Creating exchange service");
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials(userName, password);
        try {
            service.setUrl(new URI(outloookUrl));
        } catch (URISyntaxException e) {
            logger.error("Error when setting ExchangeService URL to " + outloookUrl, e);
        }
        service.setCredentials(credentials);
        service.setTraceEnabled(true);
        logger.info("Exchange service created");
        return service;
    }

    public Stream<Mail> getLatestMails(int mailsToGet) throws Exception {
        ItemView view = new ItemView(mailsToGet);
        logger.info("Getting emails...");
        final FindItemsResults<Item> items = exchangeService.findItems(WellKnownFolderName.Inbox, view);
        //MOOOOOOST IMPORTANT: load messages' properties before
        exchangeService.loadPropertiesForItems(items, PropertySet.FirstClassProperties);
        return items.getItems().stream()
                .map(item -> new Mail(getItemId(item), getItemSubject(item), getItemBody(item)));
    }

    private String getItemId(Item item) {
        try {
            return item.getId().getUniqueId();
        } catch (ServiceLocalException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String getItemSubject(Item item) {
        try {
            return item.getSubject();
        } catch (ServiceLocalException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private String getItemBody(Item item) {
        try {
            return item.getBody().toString();
        } catch (ServiceLocalException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
